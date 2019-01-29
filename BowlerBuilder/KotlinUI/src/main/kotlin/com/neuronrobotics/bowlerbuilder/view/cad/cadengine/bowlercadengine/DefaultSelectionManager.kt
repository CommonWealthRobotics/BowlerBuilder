/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.VirtualCameraDevice
import com.neuronrobotics.sdk.addons.kinematics.TransformFactory
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import eu.mihosoft.vrl.v3d.CSG
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Group
import javafx.scene.SubScene
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.transform.Affine
import tornadofx.*
import java.io.File
import java.util.Optional
import java.util.function.BiConsumer

class DefaultSelectionManager
/**
 * Manages [CSG] selections (from the GUI or programmatically).
 *
 * @param csgManager [CSGManager] to pull the CSG map from
 * @param focusGroup focus group
 * @param virtualCam virtual camera
 * @param moveCamera [BiConsumer] to move the camera around for a new selection
 */(
    private val csgManager: CSGManager,
    private val focusGroup: Group,
    private val virtualCam: VirtualCameraDevice,
    private val moveCamera: BiConsumer<TransformNR, Double>
) : SelectionManager {

    private val csgParser = CsgParser()
    private var mousePosX: Double = 0.0
    private var mousePosY: Double = 0.0
    private var mouseOldX: Double = 0.0
    private var mouseOldY: Double = 0.0
    private var mouseDeltaX: Double = 0.0
    private var mouseDeltaY: Double = 0.0
    private var previousTarget = TransformNR()
    private val selectedCSG: ObjectProperty<Optional<CSG>> = SimpleObjectProperty(Optional.empty())

    /**
     * Select all CSGs from the line in the script.
     *
     * @param script script containing CSG source
     * @param lineNumber line number in script
     */
    override fun setSelectedCSG(script: File, lineNumber: Int) {
        runLater {
            val csgs = csgParser.parseCsgFromSource(script.name, lineNumber, csgManager.getCSGs())

            if (csgs.size == 1) {
                selectCSG(csgs.iterator().next())
            } else {
                selectCSGs(csgs)
            }
        }
    }

    /**
     * Select a CSG and pan the camera to that CSG.
     *
     * @param selection CSG to select
     */
    override fun selectCSG(selection: CSG) {
        if (selectedCSG.value.isPresent && selection == selectedCSG.value.get()) {
            return
        }

        csgManager.getCSGs().forEach {
            runLater {
                csgManager.getMeshView(it)?.material = PhongMaterial(it.color)
            }
        }

        selectedCSG.value = Optional.of(selection)
        csgManager.getMeshView(selection)?.material = PhongMaterial(Color.GOLD)

        val poseToMove = TransformNR()

        if (selection.maxX < 1 || selection.minX > -1) {
            poseToMove.translateX(selection.centerX)
        }

        if (selection.maxY < 1 || selection.minY > -1) {
            poseToMove.translateY(selection.centerY)
        }

        if (selection.maxZ < 1 || selection.minZ > -1) {
            poseToMove.translateZ(selection.centerZ)
        }

        val centering = TransformFactory.nrToAffine(poseToMove)

        // this section keeps the camera oriented the same way to avoid whipping around
        val rotationOnlyComponentOfManipulator = TransformFactory.affineToNr(selection.manipulator)
        rotationOnlyComponentOfManipulator.x = 0.0
        rotationOnlyComponentOfManipulator.y = 0.0
        rotationOnlyComponentOfManipulator.z = 0.0
        val reverseRotation = rotationOnlyComponentOfManipulator.inverse()

        // TODO: Issue #32
        val startSelectNr = previousTarget.copy()
        val targetNR = if (checkManipulator(selection)) {
            TransformFactory.affineToNr(selection.manipulator)
        } else {
            TransformFactory.affineToNr(centering)
        }

        val interpolator = Affine()
        val correction = TransformFactory.nrToAffine(reverseRotation)

        runLater {
            interpolator.tx = startSelectNr.x - targetNR.x
            interpolator.ty = startSelectNr.y - targetNR.y
            interpolator.tz = startSelectNr.z - targetNR.z
            removeAllFocusTransforms()
            focusGroup.transforms.add(interpolator)

            if (checkManipulator(selection)) {
                focusGroup.transforms.add(selection.manipulator)
                focusGroup.transforms.add(correction)
            } else {
                focusGroup.transforms.add(centering)
            }

            focusInterpolate(startSelectNr, targetNR, 0, 30, interpolator)
        }
    }

    /**
     * Select all CSGs in the collection.
     *
     * @param selection CSGs to select
     */
    override fun selectCSGs(selection: Iterable<CSG>) {
        selection.forEach { csg ->
            val meshView = csgManager.getMeshView(csg)
            if (meshView != null) {
                runLater(javafx.util.Duration.millis(20.0)) {
                    meshView.material = PhongMaterial(Color.GOLD)
                }
            }
        }
    }

    /** De-select the selection.  */
    override fun cancelSelection() {
        for (key in csgManager.getCSGs()) {
            runLater {
                csgManager.getMeshView(key)?.material = PhongMaterial(key.color)
            }
        }

        selectedCSG.value = Optional.empty()
        val startSelectNr = previousTarget.copy()
        val targetNR = TransformNR()
        val interpolator = Affine()
        TransformFactory.nrToAffine(startSelectNr, interpolator)

        runLater {
            removeAllFocusTransforms()
            focusGroup.transforms.add(interpolator)
            focusInterpolate(startSelectNr, targetNR, 0, 15, interpolator)
        }
    }

    /**
     * Handle a mouse event from the 3D window.
     *
     * @param mouseEvent JavaFX-generated mouse event
     * @param csg CSG the event was generated from
     */
    override fun mouseEvent(mouseEvent: MouseEvent, csg: CSG) {
        selectCSG(csg)
        mouseOldX = mousePosX
        mouseOldY = mousePosY
        mousePosX = mouseEvent.sceneX
        mousePosY = mouseEvent.sceneY
    }

    /**
     * Attach mouse listeners to the scene. Side-effects the scene.
     *
     * @param scene the scene
     */
    override fun attachMouseListenersToScene(scene: SubScene) {
        scene.setOnMousePressed { mouseEvent ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = mouseEvent.sceneX
            mousePosY = mouseEvent.sceneY
        }

        scene.setOnMouseDragged { mouseEvent ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = mouseEvent.sceneX
            mousePosY = mouseEvent.sceneY
            mouseDeltaX = mousePosX - mouseOldX
            mouseDeltaY = mousePosY - mouseOldY

            val modifierFactor = 0.1

            val modifier = when {
                mouseEvent.isControlDown -> 0.1
                mouseEvent.isShiftDown -> 10.0
                else -> 1.0
            }

            if (mouseEvent.isPrimaryButtonDown) {
                val trans = TransformNR(
                    0.0,
                    0.0,
                    0.0,
                    RotationNR(
                        mouseDeltaY * modifierFactor * modifier * 2.0,
                        mouseDeltaX * modifierFactor * modifier * 2.0,
                        0.0
                    )
                )

                if (mouseEvent.isPrimaryButtonDown) {
                    moveCamera.accept(trans, 0.0)
                }
            } else if (mouseEvent.isSecondaryButtonDown) {
                val depth = -100 / virtualCam.zoomDepth
                moveCamera.accept(
                    TransformNR(
                        mouseDeltaX * modifierFactor * modifier * 1.0 / depth,
                        mouseDeltaY * modifierFactor * modifier * 1.0 / depth,
                        0.0,
                        RotationNR()
                    ),
                    0.0
                )
            }
        }

        scene.addEventHandler(ScrollEvent.ANY) { event ->
            if (ScrollEvent.SCROLL == event.eventType) {
                val zoomFactor = -event.deltaY * virtualCam.zoomDepth / 500
                virtualCam.zoomDepth = virtualCam.zoomDepth + zoomFactor
            }
            event.consume()
        }
    }

    private fun focusInterpolate(
        start: TransformNR,
        target: TransformNR,
        depth: Int,
        targetDepth: Int,
        interpolator: Affine
    ) {
        val depthScale = 1 - depth.toDouble() / targetDepth.toDouble()
        val sinusoidalScale = Math.sin(depthScale * (Math.PI / 2))

        val difference = start.x - target.x

        val xIncrement = difference * sinusoidalScale
        val yIncrement = (start.y - target.y) * sinusoidalScale
        val zIncrement = (start.z - target.z) * sinusoidalScale

        runLater {
            interpolator.tx = xIncrement
            interpolator.ty = yIncrement
            interpolator.tz = zIncrement
        }

        if (depth < targetDepth) {
            runLater(javafx.util.Duration.millis(20.0)) {
                focusInterpolate(start, target, depth + 1, targetDepth, interpolator)
            }
        } else {
            runLater {
                focusGroup.transforms.remove(interpolator)
            }

            previousTarget = target.copy()
            previousTarget.rotation = RotationNR()
        }
    }

    private fun removeAllFocusTransforms() = focusGroup.transforms.clear()

    private fun checkManipulator(csg: CSG) =
        Math.abs(csg.manipulator.tx) > 0.1 ||
            Math.abs(csg.manipulator.ty) > 0.1 ||
            Math.abs(csg.manipulator.tz) > 0.1
}
