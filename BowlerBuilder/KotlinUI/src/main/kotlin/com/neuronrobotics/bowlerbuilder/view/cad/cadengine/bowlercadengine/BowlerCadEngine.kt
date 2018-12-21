/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.EngineeringUnitsChangeListener
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.EngineeringUnitsSliderWidget
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.VirtualCameraDevice
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.VirtualCameraMobileBase
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.XForm
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.element.Axis3D
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.util.VirtualCameraMobileBaseFactory
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import com.neuronrobotics.imageprovider.VirtualCameraFactory
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.DepthTest
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.SceneAntialiasing
import javafx.scene.SubScene
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.CullFace
import javafx.scene.shape.DrawMode
import javafx.scene.shape.MeshView
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.stage.FileChooser
import org.apache.commons.io.FileUtils
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer
import java.util.logging.Level

/**
 * CAD Engine from BowlerStudio.
 */
@SuppressWarnings("TooGenericExceptionCaught")
class BowlerCadEngine : Pane() {

    private val csgManager = CSGManager()
    private val selectionManagerFactory = SelectionManagerFactory()
    private val scene: SubScene
    private val world = XForm()
    private val camera = PerspectiveCamera(true)

    private val root = Group()
    private val lookGroup = Group()
    private val focusGroup = Group()
    private val meshViewGroup = Group()
    private val ground = Group()
    private val axisGroup = Group()
    private val gridGroup = Group()
    private val hand = Group()

    private val axisMap = ConcurrentHashMap<MeshView, Axis3D>()
    private var virtualCam: VirtualCameraDevice? = null
    private var flyingCamera: VirtualCameraMobileBase? = null
    private var defaultCameraView: TransformNR? = null
    private val selectionManager: SelectionManager

    private val axisShowing: BooleanProperty
    private val handShowing: BooleanProperty

    init {
        axisShowing = SimpleBooleanProperty(true)
        handShowing = SimpleBooleanProperty(true)

        scene = SubScene(root, 1024.0, 1024.0, true, SceneAntialiasing.BALANCED)

        buildScene()
        buildCamera() // Initializes virtualCam which we need for selectionManager
        buildAxes()

        this.selectionManager = selectionManagerFactory.create(
            csgManager,
            focusGroup,
            virtualCam!!,
            BiConsumer { newPose, seconds ->
                this.moveCamera(
                    newPose,
                    seconds
                )
            })

        scene.fill =
            LinearGradient(
                125.0,
                0.0,
                225.0,
                0.0,
                false,
                CycleMethod.NO_CYCLE
            )

        selectionManager.attachMouseListenersToScene(scene)
        children.add(scene)

        // Clip view so it doesn't overlap with anything
        val engineClip = Rectangle()
        clip = engineClip
        layoutBoundsProperty()
            .addListener { _, _, newValue ->
                engineClip.width = newValue.width
                engineClip.height = newValue.height
            }

        axisShowing.addListener { _, _, newVal ->
            if (newVal!!) {
                showAxis()
            } else {
                hideAxis()
            }
        }

        handShowing.addListener { _, _, newVal ->
            if (newVal!!) {
                showHand()
            } else {
                hideHand()
            }
        }
    }

    /** Build the scene. Setup camera angle and add world to the root.  */
    private fun buildScene() {
        world.rotY.angle = -90.0 // point z upwards
        world.rotY.angle = 180.0 // arm out towards user
        root.children.add(world)
    }

    private fun buildCamera() {
        buildCameraStatic(camera, hand, scene)

        virtualCam = VirtualCameraDevice(camera, hand)
        VirtualCameraFactory.setFactory { virtualCam }
        flyingCamera = VirtualCameraMobileBaseFactory.create(virtualCam!!)
        defaultCameraView = flyingCamera!!.fiducialToGlobalTransform

        moveCamera(TransformNR(0.0, 0.0, 0.0, RotationNR((90 - 127).toDouble(), 24.0, 0.0)), 0.0)
    }

    /** Builds the axes.  */
    private fun buildAxes() {
        try {
            val ruler = AssetFactory.loadAsset("ruler.png")
            val groundLocal = AssetFactory.loadAsset("ground.png")
            val groundMove = Affine()
            groundMove.tx = -groundLocal.height / 2
            groundMove.ty = -groundLocal.width / 2

            val scale = 0.25
            val zRuler = Affine()
            zRuler.tz = -20 * scale
            zRuler.appendScale(scale, scale, scale)
            zRuler.appendRotation(-180.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)
            zRuler.appendRotation(-90.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
            zRuler.appendRotation(90.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
            zRuler.appendRotation(-180.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)

            val yRuler = Affine()
            yRuler.tx = -130 * scale
            yRuler.ty = -20 * scale
            yRuler.appendScale(scale, scale, scale)
            yRuler.appendRotation(180.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)
            yRuler.appendRotation(-90.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)

            val downset = Affine()
            downset.tz = 0.1

            val xRuler = Affine()
            xRuler.tx = -20 * scale
            xRuler.appendScale(scale, scale, scale)
            xRuler.appendRotation(180.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)

            val groundView = ImageView(groundLocal)
            groundView.transforms.addAll(groundMove, downset)
            groundView.opacity = 0.3

            val zrulerImage = ImageView(ruler)
            zrulerImage.transforms.addAll(zRuler, downset)

            val rulerImage = ImageView(ruler)
            rulerImage.transforms.addAll(xRuler, downset)

            val yrulerImage = ImageView(ruler)
            yrulerImage.transforms.addAll(yRuler, downset)

            runLater {
                gridGroup.children.addAll(zrulerImage, rulerImage, yrulerImage, groundView)

                val groundPlacement = Affine()
                groundPlacement.tz = -1.0
                ground.transforms.add(groundPlacement)
                focusGroup.children.add(virtualCam!!.cameraFrame)

                gridGroup.children.addAll(Axis3D(), ground)
                showAxis()
                axisGroup.children.addAll(focusGroup, meshViewGroup)
                world.children.addAll(lookGroup, axisGroup)
            }
        } catch (e: Exception) {
            LOGGER.log(
                Level.WARNING,
                "Could not load ruler/ground assets for CAD view.\n" + Throwables.getStackTraceAsString(
                    e
                )
            )
        }
    }

    /** Show the axes.  */
    private fun showAxis() {
        runLater { axisGroup.children.add(gridGroup) }
        axisMap.forEach { _, axis3D -> axis3D.show() }
    }

    /** Hide the axes.  */
    private fun hideAxis() {
        runLater { axisGroup.children.remove(gridGroup) }
        axisMap.forEach { _, axis3D -> axis3D.hide() }
    }

    private fun showHand() {
        hand.isVisible = true
    }

    private fun hideHand() {
        hand.isVisible = false
    }

    /**
     * Move the camera.
     *
     * @param newPose transform to move by
     * @param seconds seconds to move over
     */
    private fun moveCamera(newPose: TransformNR, seconds: Double) =
        flyingCamera!!.DriveArc(newPose, seconds)

    /** Home the camera to its default view.  */
    fun homeCamera() {
        flyingCamera!!.setGlobalToFiducialTransform(defaultCameraView)
        virtualCam!!.zoomDepth = VirtualCameraDevice.getDefaultZoomDepth().toDouble()
        flyingCamera!!.updatePositions()
    }

    fun getCsgMap() = csgManager.getCsgToMeshView()

    /**
     * Select all CSGs from the line in the script.
     *
     * @param script script containing CSG source
     * @param lineNumber line number in script
     */
    fun setSelectedCSG(script: File, lineNumber: Int) =
        selectionManager.setSelectedCSG(script, lineNumber)

    /**
     * Select a CSG.
     *
     * @param selection CSG to select
     */
    fun selectCSG(selection: CSG) = selectionManager.selectCSG(selection)

    /**
     * Select all CSGs in the collection.
     *
     * @param selection CSGs to select
     */
    fun selectCSGs(selection: Iterable<CSG>) = selectionManager.selectCSGs(selection)

    /**
     * Add a CSG to the scene graph.
     *
     * @param csg CSG to add
     */
    fun addCSG(csg: CSG) {
        if (csgManager.has(csg)) {
            return
        }

        val mesh = csg.mesh
        mesh.material = PhongMaterial(csg.color)
        mesh.depthTest = DepthTest.ENABLE
        mesh.cullFace = CullFace.BACK

        if (csg.name != null && "" != csg.name && csgManager.has(csg.name)) {
            val meshView = csgManager.getMeshView(csg.name)
            if (meshView != null) {
                mesh.drawMode = meshView.drawMode
            }
        } else {
            mesh.drawMode = DrawMode.FILL
        }

        mesh.setOnMouseClicked { mouseEvent ->
            if (mouseEvent.button == MouseButton.PRIMARY) {
                selectionManager.mouseEvent(mouseEvent, csg)
            } else if (mouseEvent.button == MouseButton.SECONDARY) {
                val menu = ContextMenu()
                menu.isAutoHide = true

                // Wireframe/Solid draw toggle
                val wireframe = if (mesh.drawMode == DrawMode.LINE) {
                    MenuItem("Show As Solid")
                } else {
                    MenuItem("Show As Wireframe")
                }

                // Set the title of the MenuItem to the opposite of the current draw

                // Set the onAction of the MenuItem to flip the draw state
                wireframe.setOnAction {
                    if (mesh.drawMode == DrawMode.FILL) {
                        mesh.drawMode = DrawMode.LINE
                        wireframe.text = "Show As Solid"
                    } else {
                        mesh.drawMode = DrawMode.FILL
                        wireframe.text = "Show As Wireframe"
                    }
                }

                val params = csg.parameters
                if (params != null) {
                    val parameters = Menu("Parameters")

                    params.forEach { key ->
                        // Regenerate all objects if their parameters have changed
                        val regenerateObjects = {
                            // Get the set of objects to check for regeneration after the initial
                            // regeneration cycle
                            val objects = getCsgMap().keys

                            // Hide the menu because the parameter is done being changed
                            menu.hide()

                            fireRegenerate(key, objects)
                        }

                        val param = CSGDatabase.get(key)
                        csg.setParameterIfNull(key)

                        if (param is LengthParameter) {
                            val widget = EngineeringUnitsSliderWidget(
                                object : EngineeringUnitsChangeListener {
                                    override fun onSliderMoving(
                                        sliderWidget: EngineeringUnitsSliderWidget,
                                        newAngleDegrees: Double
                                    ) {
                                        try {
                                            csg.setParameterNewValue(key, newAngleDegrees)
                                        } catch (e: Exception) {
                                            LOGGER.log(
                                                Level.SEVERE,
                                                "Could not set new parameter value.\n"
                                                    + Throwables.getStackTraceAsString(e)
                                            )
                                        }
                                    }

                                    override fun onSliderDoneMoving(
                                        sliderWidget: EngineeringUnitsSliderWidget,
                                        newAngleDegrees: Double
                                    ) {
                                        regenerateObjects()
                                    }
                                },
                                java.lang.Double.parseDouble(param.options[1]),
                                java.lang.Double.parseDouble(param.options[0]),
                                param.mm,
                                400.0,
                                key
                            )

                            val customMenuItem = CustomMenuItem(widget)
                            customMenuItem.isHideOnClick = false // Regen will hide the menu
                            parameters.items.add(customMenuItem)
                        } else {
                            if (param != null) {
                                val paramTypes = Menu(param.name + " " + param.strValue)

                                param
                                    .options
                                    .forEach { option ->
                                        val customMenuItem = MenuItem(option)
                                        customMenuItem.setOnAction { event ->
                                            param.strValue = option
                                            CSGDatabase.get(param.name).strValue = option
                                            CSGDatabase.getParamListeners(param.name)
                                                .forEach { listener ->
                                                    listener.parameterChanged(
                                                        param.name, param
                                                    )
                                                }
                                            regenerateObjects()
                                        }

                                        paramTypes.items.add(customMenuItem)
                                    }

                                parameters.items.add(paramTypes)
                            }
                        }
                    }

                    menu.items.add(parameters)
                }

                val exportSTL = MenuItem("Export as STL")
                exportSTL.setOnAction {
                    val chooser = FileChooser()
                    var save: File? = chooser.showSaveDialog(root.scene.window)
                    if (save != null) {
                        if (!save.path.endsWith(".stl")) {
                            save = File(save.absolutePath + ".stl")
                        }

                        val readyCSG = csg.prepForManufacturing()
                        try {
                            FileUtils.write(save, readyCSG.toStlString())
                        } catch (e: IOException) {
                            LOGGER.log(
                                Level.SEVERE,
                                "Could not write CSG STL String.\n" + Throwables.getStackTraceAsString(
                                    e
                                )
                            )
                        }
                    }
                }

                menu.items.addAll(wireframe, exportSTL)
                // Need to set the root as mesh.getScene().getWindow() so setAutoHide()
                // works when we
                // right-click somewhere else
                mesh.setOnContextMenuRequested { event ->
                    menu.show(
                        mesh.scene.window,
                        event.screenX,
                        event.screenY
                    )
                }
            }
        }

        // TODO: Figure out how to cancel selection on a key press
        mesh.addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            LOGGER.info("key event: " + keyEvent.code.getName())
            if (KeyCode.ESCAPE == keyEvent.code) {
                LOGGER.info("hit escape")
                selectionManager.cancelSelection()
            }
        }

        runLater {
            try {
                if (!meshViewGroup.children.contains(mesh)) {
                    meshViewGroup.children.add(mesh)
                }
            } catch (e: IllegalArgumentException) {
                LOGGER.warning("Possible duplicate child added to CAD engine.")
                LOGGER.fine(Throwables.getStackTraceAsString(e))
            }
        }

        csgManager.addCSG(csg, mesh)
        LOGGER.log(Level.FINE, "Added CSG with name: " + csg.name)
    }

    fun addAllCSGs(vararg csgs: CSG) = csgs.forEach { addCSG(it) }

    fun addAllCSGs(csgs: Iterable<CSG>) = csgs.forEach { addCSG(it) }.also {
        println(getCsgMap().keys.joinToString { it.name })
    }

    fun clearCSGs() {
        try {
            FxUtil.runFXAndWait { meshViewGroup.children.clear() }
        } catch (e: InterruptedException) {
            LOGGER.fine(Throwables.getStackTraceAsString(e))
        }

        csgManager.clearCSGs()
    }

    fun axisShowingProperty() = axisShowing

    fun handShowingProperty() = handShowing

    fun getView() = this

    fun getSubScene() = scene

    private fun fireRegenerate(key: String, currentObjectsToCheck: Set<CSG>) {
        val thread = LoggerUtilities.newLoggingThread(LOGGER) {
            val toAdd = ArrayList<CSG>()
            val toRemove = ArrayList<CSG>()

            // For each parameter of each object
            currentObjectsToCheck.forEach {
                it.parameters.forEach { param ->
                    // If the parameter matches the input
                    if (param!!.contentEquals(key) && !toRemove.contains(it)) {
                        // Regen the csg, remove the existing CSG, and add the new CSG
                        val regen = it.regenerate()
                        toRemove.add(it)
                        toAdd.add(regen)
                    }
                }
            }

            runLater { toRemove.forEach { meshViewGroup.children.remove(it.mesh) } }

            runLater { addAllCSGs(toAdd) }

            LOGGER.info { "Saving CSG database" }
            CSGDatabase.saveDatabase()
            LOGGER.log(Level.INFO, "Done saving CSG database")
        }

        thread.name = "CAD Regenerate Thread"
        thread.start()
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(BowlerCadEngine::class.java.simpleName)

        private fun buildCameraStatic(camera: PerspectiveCamera, hand: Group, scene: SubScene) {
            camera.nearClip = .1
            camera.farClip = 100000.0
            scene.camera = camera

            camera.rotationAxis = Rotate.Z_AXIS
            camera.rotate = 180.0

            val cylinder = Cylinder(
                0.0,
                5.0,
                20.0,
                20
            ).toCSG().roty(90.0).setColor(Color.BLACK)
            hand.children.add(cylinder.mesh)
        }
    }
}
