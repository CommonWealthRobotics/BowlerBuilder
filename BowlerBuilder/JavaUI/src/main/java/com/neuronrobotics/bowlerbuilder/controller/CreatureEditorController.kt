/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.common.base.Throwables
import com.google.inject.Inject
import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.BowlerKernelUtilities
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.module.LimbLayoutControllerModule
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.LimbLayoutController
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.LimbLinkLayoutController
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.ConfigTabLimbSelection
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.LimbTabLimbSelection
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.MovementTabLimbSelection
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.ScriptTabLimbSelection
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.ConfigTabLinkSelection
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.MovementTabLinkSelection
import com.neuronrobotics.bowlerbuilder.model.LimbType
import com.neuronrobotics.bowlerbuilder.model.LinkData
import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesConsumer
import com.neuronrobotics.bowlerbuilder.model.preferences.bowler.CreatureEditorControllerPreferencesService
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.FullBodyJogWidget
import com.neuronrobotics.bowlerbuilder.view.dialog.AddLimbDialog
import com.neuronrobotics.bowlerbuilder.view.dialog.GistFileSelectionDialog
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.common.DeviceManager
import com.neuronrobotics.sdk.util.ThreadUtil
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextInputDialog
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.util.Callback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.controlsfx.control.Notifications
import org.eclipse.jgit.api.errors.GitAPIException
import org.kohsuke.github.GHGist
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.util.Arrays
import java.util.Optional
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.logging.Level
import kotlin.concurrent.thread

class CreatureEditorController
@Inject constructor(
    private val mainWindowController: MainWindowController,
    private val preferencesService: CreatureEditorControllerPreferencesService
) : PreferencesConsumer {

    @FXML
    lateinit var cadProgress: ProgressIndicator
    @FXML
    private lateinit var autoRegenCAD: CheckBox
    @FXML
    private lateinit var regenCADButton: Button
    @FXML
    private lateinit var genPrintableCAD: Button
    @FXML
    private lateinit var genKinSTL: Button
    @FXML
    private lateinit var creatureTabPane: TabPane
    @FXML
    private lateinit var limbTab: Tab
    @FXML
    private lateinit var movementTab: Tab
    @FXML
    private lateinit var scriptTab: Tab
    @FXML
    private lateinit var configTab: Tab

    private val limbWidget: AnchorPane = AnchorPane()
    private val movementWidget: AnchorPane = AnchorPane()
    private val configWidget: AnchorPane = AnchorPane()
    private val scriptWidget: AnchorPane = AnchorPane()
    private val widgetSelectionProperty: ObjectProperty<Selection> = SimpleObjectProperty()
    private val selectedWidgetPane: ObjectProperty<AnchorPane> = SimpleObjectProperty()

    private var device: MobileBase? = null
    private var cadManager: MobileBaseCadManager? = null
    private var controller: AceCreatureLabController? = null

    @FXML
    private fun initialize() {
        autoRegenCAD
            .selectedProperty()
            .addListener { _, _, newValue ->
                cadManager?.autoRegen = newValue
            }

        selectedWidgetPane.set(limbWidget) // Limb widget to start

        // Change the widget pane that new widgets go into when the user changes tabs
        creatureTabPane
            .selectionModel
            .selectedItemProperty()
            .addListener { _, _, newValue ->
                when {
                    newValue === limbTab -> selectedWidgetPane.set(limbWidget)
                    newValue === movementTab -> selectedWidgetPane.set(movementWidget)
                    newValue === configTab -> selectedWidgetPane.set(configWidget)
                    newValue === scriptTab -> selectedWidgetPane.set(scriptWidget)
                }
            }

        // Fill the widget pane with the widget for the selection
        widgetSelectionProperty.addListener { _, _, newValue ->
            if (newValue != null) {
                val widgetPane = selectedWidgetPane.get()
                widgetPane?.children?.setAll(newValue.getWidget())
            }
        }

        val padding = "-fx-padding: 5px;"

        limbTab.graphic = AssetFactory.loadIcon("creature.png")
        limbTab.style = padding
        limbTab.tooltip = Tooltip("Limb Configuration")

        movementTab.graphic = AssetFactory.loadIcon("Move-Limb.png")
        movementTab.style = padding
        movementTab.tooltip = Tooltip("Movement")

        configTab.graphic = AssetFactory.loadIcon("Advanced-Configuration.png")
        configTab.style = padding
        configTab.tooltip = Tooltip("Hardware Configuration")

        scriptTab.graphic = AssetFactory.loadIcon("Edit-Script.png")
        scriptTab.style = padding
        scriptTab.tooltip = Tooltip("Scripting")

        regenCADButton.graphic = AssetFactory.loadIcon("Generate-Cad.png")
        regenCADButton.text = "Regenerate CAD"

        genPrintableCAD.graphic = AssetFactory.loadIcon("Printable-Cad.png")
        genPrintableCAD.text = "Printable CAD"

        genKinSTL.graphic = AssetFactory.loadIcon("Printable-Cad.png")
        genKinSTL.text = "Kinematic STL"

        refreshPreferences()
    }

    override fun refreshPreferences() =
        autoRegenCAD
            .selectedProperty()
            .set(preferencesService.getCurrentPreferencesOrDefault().autoRegenCAD)

    override fun getCurrentPreferences(): Preferences =
        preferencesService.getCurrentPreferencesOrDefault()

    /**
     * Fill the CreatureLab tabs with menus for a [MobileBase].
     *
     * @param device [MobileBase] to load menus from
     * @param cadManager [MobileBaseCadManager] to trigger CAD regens to
     * @param controller [AceCreatureLabController] to load files into
     */
    fun generateMenus(
        device: MobileBase,
        cadManager: MobileBaseCadManager,
        controller: AceCreatureLabController
    ) {
        this.device = device
        this.cadManager = cadManager
        this.controller = controller

        cadManager.autoRegen = autoRegenCAD.isSelected

        generateLimbTab(device)
        generateMovementTab(device)
        generateConfigTab(device, cadManager)
        generateScriptTab(device, controller)
    }

    /** Regenerate menus using the parameters from the last time generateMenus() was called.  */
    fun regenerateMenus() = generateMenus(device!!, cadManager!!, controller!!)

    /** Clear the selected widget.  */
    fun clearWidget() {
        widgetSelectionProperty.value = null
        selectedWidgetPane.get().children.clear()
    }

    private fun generateLimbTab(mobileBase: MobileBase) {
        val loader = createLimbLayoutFxmlLoader(
            mobileBase,
            "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"
        )

        try {
            val content = loader.load<Node>()
            GlobalScope.launch(Dispatchers.JavaFx) {
                limbTab.content = getScrollPane(VBox(10.0, content, limbWidget))
            }

            val controller = loader.getController<LimbLayoutController>()

            controller
                .limbSelectionProperty()
                .addListener { _, _, newValue ->
                    newValue.ifPresent { limb ->
                        widgetSelectionProperty.set(
                            LimbTabLimbSelection(mobileBase, limb, this)
                        )
                    }
                }

            GlobalScope.launch(Dispatchers.JavaFx) {
                controller.addToLegHBox(
                    getAddLinkButton(
                        AssetFactory.loadIcon("Add-Leg.png"),
                        LimbType.LEG,
                        mobileBase
                    )
                )

                controller.addToArmHBox(
                    getAddLinkButton(
                        AssetFactory.loadIcon("Add-Arm.png"),
                        LimbType.ARM,
                        mobileBase
                    )
                )

                controller.addToSteerableHBox(
                    getAddLinkButton(
                        AssetFactory.loadIcon("Add-Steerable-Wheel.png"),
                        LimbType.STEERABLE_WHEEL,
                        mobileBase
                    )
                )

                controller.addToFixedHBox(
                    getAddLinkButton(
                        AssetFactory.loadIcon("Add-Fixed-Wheel.png"),
                        LimbType.FIXED_WHEEL,
                        mobileBase
                    )
                )
            }
        } catch (e: IOException) {
            LOGGER.severe(
                """
                Could not load LimbLayout.
                ${Throwables.getStackTraceAsString(e)}
                """.trimIndent()
            )
        }
    }

    private fun getAddLinkButton(
        icon: ImageView,
        limbType: LimbType,
        mobileBase: MobileBase
    ) = Button().apply {
        graphic = icon
        tooltip = Tooltip("Add " + limbType.tooltipName)
        setOnAction {
            when (limbType) {
                LimbType.LEG -> promptAndAddLimb(
                    LimbType.LEG.defaultFileName,
                    mobileBase,
                    mobileBase.legs
                )

                LimbType.ARM -> promptAndAddLimb(
                    LimbType.ARM.defaultFileName,
                    mobileBase,
                    mobileBase.appendages
                )

                LimbType.FIXED_WHEEL -> promptAndAddLimb(
                    LimbType.FIXED_WHEEL.defaultFileName, mobileBase, mobileBase.drivable
                )

                LimbType.STEERABLE_WHEEL -> promptAndAddLimb(
                    LimbType.STEERABLE_WHEEL.defaultFileName, mobileBase, mobileBase.steerable
                )
            }
        }
    }

    private fun generateMovementTab(mobileBase: MobileBase) {
        val loader = createLimbLayoutFxmlLoader(
            mobileBase,
            "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLinkLayout.fxml"
        )

        try {
            val content = loader.load<Node>()
            val container = VBox(10.0, FullBodyJogWidget(mobileBase).view, content, movementWidget)
            container.maxWidth(java.lang.Double.MAX_VALUE)
            content.maxWidth(java.lang.Double.MAX_VALUE)

            GlobalScope.launch(Dispatchers.JavaFx) {
                movementTab.content = getScrollPane(container)
            }

            val controller = loader.getController<LimbLinkLayoutController>()

            controller
                .limbSelectionProperty()
                .addListener { _, _, newValue ->
                    newValue.ifPresent { limb ->
                        widgetSelectionProperty.set(
                            MovementTabLimbSelection(limb)
                        )
                    }
                    controller.linkSelectionProperty().setValue(Optional.empty<LinkData>())
                }

            controller
                .linkSelectionProperty()
                .addListener { _, _, newValue ->
                    newValue.ifPresent { linkData ->
                        widgetSelectionProperty.set(
                            MovementTabLinkSelection(linkData)
                        )
                    }
                }
        } catch (e: IOException) {
            LOGGER.severe(
                """
                Could not load LimbLinkLayout.
                ${Throwables.getStackTraceAsString(e)}
                """.trimIndent()
            )
        }
    }

    private fun generateConfigTab(
        mobileBase: MobileBase,
        mobileBaseCadManager: MobileBaseCadManager
    ) {
        val loader = createLimbLayoutFxmlLoader(
            mobileBase,
            "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLinkLayout.fxml"
        )

        try {
            val content = loader.load<Node>()
            val container = VBox(10.0, content, configWidget)
            container.maxWidth(java.lang.Double.MAX_VALUE)
            content.maxWidth(java.lang.Double.MAX_VALUE)

            GlobalScope.launch(Dispatchers.JavaFx) { configTab.content = getScrollPane(container) }

            val controller = loader.getController<LimbLinkLayoutController>()

            controller
                .limbSelectionProperty()
                .addListener { _, _, newValue ->
                    newValue.ifPresent { limb ->
                        widgetSelectionProperty.set(
                            ConfigTabLimbSelection(
                                limb,
                                mobileBaseCadManager
                            )
                        )
                    }
                    controller.linkSelectionProperty().setValue(Optional.empty<LinkData>())
                }

            controller
                .linkSelectionProperty()
                .addListener { _, _, newValue ->
                    newValue.ifPresent { (parentLimb, _, dhLink, linkConfiguration) ->
                        widgetSelectionProperty.set(
                            ConfigTabLinkSelection(
                                dhLink,
                                linkConfiguration,
                                parentLimb,
                                mobileBaseCadManager
                            )
                        )
                    }
                }
        } catch (e: IOException) {
            LOGGER.severe(
                """
                Could not load LimbLinkLayout.
                ${Throwables.getStackTraceAsString(e)}
                """.trimIndent()
            )
        }
    }

    private fun generateScriptTab(
        mobileBase: MobileBase,
        creatureLabController: AceCreatureLabController
    ) {
        fun handleException(e: Exception, gitXMLSource: Array<String>) {
            LOGGER.severe(
                """
                Could not parse creature file from source: ${Arrays.toString(gitXMLSource)}
                ${Throwables.getStackTraceAsString(e)}
                """.trimIndent()
            )

            GlobalScope.launch(Dispatchers.JavaFx) {
                Notifications.create()
                    .title("Error")
                    .text("Could not parse file from git source. Creature loading stopped.")
                    .showError()
            }
        }

        val makeCopy = Button("Clone Creature")
        makeCopy.graphic = AssetFactory.loadIcon("Make-Copy-of-Creature.png")
        makeCopy.setOnAction {
            GlobalScope.launch(Dispatchers.JavaFx) {
                val oldName = mobileBase.scriptingName
                val dialog = TextInputDialog("${oldName}_copy").apply {
                    title = "Make a copy of $oldName"
                    headerText = "Set the scripting name for this creature"
                    contentText = "Name of the new creature:"
                }

                dialog.showAndWait().ifPresent { name ->
                    thread(start = true) { cloneCreature(mainWindowController, mobileBase, name) }
                }
            }
        }

        // Have to declare these here because the following block is deeper scope
        val topLevelControls = GridPane()
        topLevelControls.padding = Insets(5.0)
        topLevelControls.add(makeCopy, 0, 0)

        val gitXMLSource = mobileBase.gitSelfSource
        try {
            val deviceXMLFile = ScriptingEngine.fileFromGit(gitXMLSource[0], gitXMLSource[1])

            if (ScriptingEngine.checkOwner(deviceXMLFile)) {
                val loader = createLimbLayoutFxmlLoader(
                    mobileBase,
                    "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"
                )

                val tabContent = getScriptTabContentAsDeviceOwner(
                    makeCopy,
                    deviceXMLFile,
                    mobileBase,
                    creatureLabController
                )

                try {
                    val content = loader.load<Node>()
                    val controller = loader.getController<LimbLayoutController>()

                    controller
                        .limbSelectionProperty()
                        .addListener { _, _, newValue ->
                            newValue.ifPresent { limb ->
                                widgetSelectionProperty.set(
                                    ScriptTabLimbSelection(limb, creatureLabController)
                                )
                            }
                        }

                    GlobalScope.launch(Dispatchers.JavaFx) {
                        scriptTab.content =
                            getScrollPane(VBox(5.0, tabContent, content, scriptWidget))
                    }
                } catch (e: IOException) {
                    LOGGER.severe(
                        """
                        Could not load LimbLayout.
                        ${Throwables.getStackTraceAsString(e)}
                        """.trimIndent()
                    )

                    GlobalScope.launch(Dispatchers.JavaFx) {
                        scriptTab.content = getScrollPane(VBox(5.0, tabContent))
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.JavaFx) {
                    scriptTab.content = getScrollPane(VBox(5.0, topLevelControls))
                }
            }
        } catch (e: GitAPIException) {
            handleException(e, gitXMLSource)
        } catch (e: IOException) {
            handleException(e, gitXMLSource)
        }
    }

    /**
     * Wrap a [Node] in a [ScrollPane].
     *
     * @param node node to wrap
     * @return scroll pane with node
     */
    private fun getScrollPane(node: Node) =
        ScrollPane(node).apply {
            isFitToWidth = true
            padding = Insets(5.0)
        }

    /**
     * Prompt with a dialog for limb name and hardware indices, then add the limb.
     *
     * @param defaultFileName filename in gist for default configuration (use [LimbType]
     * @param device [MobileBase] to query used hardware channels from
     * @param toAdd list to add the new limb to
     */
    private fun promptAndAddLimb(
        defaultFileName: String,
        device: MobileBase,
        toAdd: MutableList<DHParameterKinematics>
    ) {
        try {
            val xmlContent = ScriptingEngine.codeFromGit(
                "https://gist.github.com/d11d69722610930ae1db9e5821a26178.git", defaultFileName
            )[0]
            val newLeg = DHParameterKinematics(null, IOUtils.toInputStream(xmlContent, "UTF-8"))

            val linkConfigurations = newLeg.linkConfigurations
            val dialog = AddLimbDialog(
                newLeg.scriptingName,
                linkConfigurations.size,
                BowlerKernelUtilities.getTakenHardwareChannels(device)
            )

            dialog
                .showAndWait()
                .ifPresent { (name, indices) ->
                    newLeg.scriptingName = name

                    for (i in linkConfigurations.indices) {
                        val conf = linkConfigurations[i]
                        conf.hardwareIndex = indices[i]
                        newLeg.factory.refreshHardwareLayer(conf)
                    }

                    toAdd.add(newLeg)
                    regenerateMenus()
                }
        } catch (e: Exception) {
            LOGGER.warning(
                """
                Could not add limb.
                ${Throwables.getStackTraceAsString(e)}
                """.trimIndent()
            )
        }
    }

    @FXML
    private fun onRegenCAD(actionEvent: ActionEvent) = regenCAD(true)

    @FXML
    private fun onGenPrintableCAD(actionEvent: ActionEvent) = genSTLs(device!!, cadManager!!, false)

    @FXML
    private fun onGenKinSTL(actionEvent: ActionEvent) = genSTLs(device!!, cadManager!!, true)

    /**
     * Regenerate [MobileBase] CAD if there is a non-null [MobileBaseCadManager].
     *
     * @param force whether to ignore the state of auto-regen
     */
    @JvmOverloads
    fun regenCAD(force: Boolean = false) {
        // TODO: Re-enable this after it's put into the kernel
        // cadManager.generateCad(force);
        cadManager!!.generateCad()
    }

    /**
     * Show a [DirectoryChooser] to pick a save directory and then generate and save STL files
     * for the given [MobileBase] and [MobileBaseCadManager].
     *
     * @param device creature to gen STLs for
     * @param cadManager CAD manager to gen STLs with
     * @param isKinematic whether to gen kinematic STLs
     */
    private fun genSTLs(
        device: MobileBase,
        cadManager: MobileBaseCadManager,
        isKinematic: Boolean
    ) {
        val defaultStlDir = File(System.getProperty("user.home") + "/bowler-workspace/STL/")

        if (!defaultStlDir.exists() && !defaultStlDir.mkdirs()) {
            LOGGER.log(Level.WARNING, "Could not create default directory to save STL files.")
            return
        }

        GlobalScope.launch(Dispatchers.JavaFx) {
            val chooser = DirectoryChooser().apply {
                title = "Select Output Directory For STL files"
                initialDirectory = defaultStlDir
            }

            val baseDirForFiles = chooser.showDialog(creatureTabPane.scene.window)
            if (baseDirForFiles == null) {
                LOGGER.log(Level.INFO, "No directory selected. Not saving STL files.")
                return@launch
            }

            LoggerUtilities.newLoggingThread(LOGGER) {
                try {
                    val files = cadManager.generateStls(device, baseDirForFiles, isKinematic)

                    GlobalScope.launch(Dispatchers.JavaFx) {
                        Notifications.create()
                            .title("STL Export Success")
                            .text(
                                """
                                All STL files for the creature generated at:
                                ${files[0].absolutePath}
                                """.trimIndent()
                            )
                            .showInformation()
                    }
                } catch (e: IOException) {
                    LOGGER.log(
                        Level.WARNING,
                        """
                        Could not generate STL files to save in: ${baseDirForFiles.absolutePath}
                        ${Throwables.getStackTraceAsString(e)}
                        """.trimIndent()
                    )

                    GlobalScope.launch(Dispatchers.JavaFx) {
                        Notifications.create()
                            .title("STL Export Failure")
                            .text("Could not generate STL files.")
                            .showError()
                    }
                } catch (e: RuntimeException) {
                    e.message?.let {
                        if (it.contains("IgenerateBed")) {
                            LOGGER.log(
                                Level.INFO,
                                """
                                Cannot generate STL files because the supplied CAD manager
                                does not implement the IgenerateBed interface.
                                ${Throwables.getStackTraceAsString(e)}
                                """.trimIndent()
                            )
                        }
                    } ?: LOGGER.log(Level.WARNING, Throwables.getStackTraceAsString(e))

                    GlobalScope.launch(Dispatchers.JavaFx) {
                        Notifications.create()
                            .title("STL Export Failure")
                            .text("Could not generate STL files.")
                            .showError()
                    }
                }
            }.start()
        }
    }

    companion object {

        private val LOGGER =
            LoggerUtilities.getLogger(CreatureEditorController::class.java.simpleName)

        private fun getScriptTabContentAsDeviceOwner(
            makeCopy: Button,
            deviceXMLFile: File,
            device: MobileBase,
            controller: AceCreatureLabController
        ): GridPane {
            val publish = Button("Publish").apply {
                graphic = AssetFactory.loadIcon("Publish.png")
                setOnAction {
                    PublishDialog()
                        .showAndWait()
                        .ifPresent { commitMessage ->
                            publishCreature(
                                device,
                                deviceXMLFile,
                                commitMessage
                            )
                        }
                }
            }

            val editWalkingEngine = createEditScriptButton(
                "Edit Walking Engine",
                "Edit-Walking-Engine.png",
                "Walking Engine",
                device.gitWalkingEngine,
                controller
            )

            val editCADEngine = createEditScriptButton(
                "Edit CAD Engine",
                "Edit-CAD-Engine.png",
                "CAD Engine",
                device.gitCadEngine,
                controller
            )

            val setWalkingEngine = createSetEngineButton(
                "Set Walking Engine",
                "Set-Walking-Engine.png",
                "Select Walking Engine",
                Consumer { device.gitWalkingEngine = it })

            val setCADEngine = createSetEngineButton(
                "Set CAD Engine",
                "Set-CAD-Engine.png",
                "Select CAD Engine",
                Consumer { device.gitCadEngine = it })

            GridPane.setHalignment(makeCopy, HPos.RIGHT)
            GridPane.setHalignment(editWalkingEngine, HPos.RIGHT)
            GridPane.setHalignment(setWalkingEngine, HPos.RIGHT)

            return GridPane().apply {
                padding = Insets(5.0)
                vgap = 5.0
                hgap = 5.0

                add(makeCopy, 0, 0)
                add(publish, 1, 0)
                add(editWalkingEngine, 0, 1)
                add(editCADEngine, 1, 1)
                add(setWalkingEngine, 0, 2)
                add(setCADEngine, 1, 2)
            }
        }

        /**
         * Create a Button to edit a script.
         *
         * @param buttonTitle button text
         * @param scriptIconName button icon file name
         * @param scriptFileName script file name
         * @param fileInGit file URL and name for [ScriptingEngine]
         * @param controller controller to load the script
         * @return the Button
         */
        private fun createEditScriptButton(
            buttonTitle: String,
            scriptIconName: String,
            scriptFileName: String,
            fileInGit: Array<String>,
            controller: AceCreatureLabController
        ) = Button(buttonTitle).apply {
            graphic = AssetFactory.loadIcon(scriptIconName)
            setOnAction {
                tryParseCreatureFile(fileInGit[0], fileInGit[1])
                    .ifPresent { file1 ->
                        controller.loadFileIntoNewTab(
                            scriptFileName,
                            AssetFactory.loadIcon(scriptIconName),
                            fileInGit[0],
                            fileInGit[1],
                            file1
                        )
                    }
            }
        }

        /**
         * Create a Button to set an engine script.
         *
         * @param buttonTitle button text
         * @param scriptIconName button icon file name
         * @param dialogTitle [GistFileSelectionDialog] title
         * @param setEngine [GistFileSelectionDialog] result consumer to set the engine script
         * @return the Button
         */
        private fun createSetEngineButton(
            buttonTitle: String,
            scriptIconName: String,
            dialogTitle: String,
            setEngine: Consumer<in Array<String>>
        ) = Button(buttonTitle).apply {
            graphic = AssetFactory.loadIcon(scriptIconName)
            setOnAction {
                GistFileSelectionDialog(dialogTitle, Predicate { file -> !file.endsWith(".xml") })
                    .showAndWait()
                    .ifPresent(setEngine)
            }
        }

        /**
         * Try to get a [File] from a "file in git".
         *
         * @param remoteURI file URL
         * @param fileInRepo file name
         * @return the file
         */
        private fun tryParseCreatureFile(
            remoteURI: String,
            fileInRepo: String
        ): Optional<File> {
            fun handleException(e: Exception) {
                LOGGER.severe(
                    """
                    Could not parse creature file from source. Creature loading stopped.
                    URL: $remoteURI
                    Filename: $fileInRepo
                    ${Throwables.getStackTraceAsString(e)}
                    """.trimIndent()
                )

                GlobalScope.launch(Dispatchers.JavaFx) {
                    Notifications.create()
                        .title("Error")
                        .text("Could not parse file from git source. Creature loading stopped.")
                        .showError()
                }
            }

            try {
                return Optional.of(ScriptingEngine.fileFromGit(remoteURI, fileInRepo))
            } catch (e: GitAPIException) {
                handleException(e)
            } catch (e: IOException) {
                handleException(e)
            }

            return Optional.empty()
        }

        /**
         * Make a clone of a creature. Loads the new creature in a new tab and refreshes git menus.
         *
         * @param mainWindowController controller to load the creature in
         * @param device creature
         * @param name new (clone) creature name
         */
        private fun cloneCreature(
            mainWindowController: MainWindowController,
            device: MobileBase,
            name: String
        ) {
            LOGGER.log(Level.INFO, "Your new creature: $name")
            device.scriptingName = name

            val github = ScriptingEngine.getGithub()
            val builder = github.createGist()
            builder.description(name)
            val filename = "$name.xml"
            builder.file(filename, "<none>")
            builder.public_(true)
            val gist: GHGist
            try {
                gist = builder.create()
                val gitURL =
                    "https://gist.github.com/${ScriptingEngine.urlToGist(gist.htmlUrl)}.git"

                LOGGER.log(Level.INFO, "Creating new Robot repo.")
                while (true) {
                    try {
                        ScriptingEngine.fileFromGit(gitURL, filename)
                        break
                    } catch (ignored: Exception) {
                        LOGGER.log(Level.INFO, "Waiting. $gist not built yet.")
                    }

                    ThreadUtil.wait(500)
                }
                LOGGER.log(Level.INFO, "Creating Gist at: $gitURL")

                LOGGER.log(Level.INFO, "Copying CAD engine.")
                device.gitCadEngine = ScriptingEngine.copyGitFile(
                    device.gitCadEngine[0], gitURL, device.gitCadEngine[1]
                )

                LOGGER.log(
                    Level.INFO,
                    "Copying walking engine. Was: ${Arrays.toString(device.gitWalkingEngine)}"
                )
                device.gitWalkingEngine = ScriptingEngine.copyGitFile(
                    device.gitWalkingEngine[0], gitURL, device.gitWalkingEngine[1]
                )

                LOGGER.log(
                    Level.INFO, "Walking engine is now: ${Arrays.toString(device.gitWalkingEngine)}"
                )
                for (dh in device.allDHChains) {
                    LOGGER.log(
                        Level.INFO,
                        "Copying leg CAD engine: ${Arrays.toString(dh.gitCadEngine)}"
                    )
                    dh.gitCadEngine =
                        ScriptingEngine.copyGitFile(dh.gitCadEngine[0], gitURL, dh.gitCadEngine[1])

                    LOGGER.log(Level.INFO, "Copying leg DH engine.")
                    dh.gitDhEngine =
                        ScriptingEngine.copyGitFile(dh.gitDhEngine[0], gitURL, dh.gitDhEngine[1])
                }

                val xml = device.xml
                ScriptingEngine.pushCodeToGit(
                    gitURL,
                    ScriptingEngine.getFullBranch(gitURL),
                    filename,
                    xml,
                    "new Robot content"
                )

                LOGGER.info("Clone finished.")
                GlobalScope.launch(Dispatchers.JavaFx) {
                    Notifications.create()
                        .title("Clone Finished")
                        .text("The creature cloning operation finished successfully.")
                        .show()
                }

                val mobileBase = MobileBase(IOUtils.toInputStream(xml, "UTF-8"))
                mobileBase.gitSelfSource = arrayOf(gitURL, "$name.xml")
                device.disconnect()

                DeviceManager.addConnection(mobileBase, mobileBase.scriptingName)
                val selfSource = mobileBase.gitSelfSource
                mainWindowController.loadCreatureLab(selfSource)
                mainWindowController.reloadGitMenus()
            } catch (e: MalformedURLException) {
                LOGGER.log(
                    Level.SEVERE,
                    """
                    Could not make copy of creature. Malformed url.
                    ${Throwables.getStackTraceAsString(e)}
                    """.trimIndent()
                )

                GlobalScope.launch(Dispatchers.JavaFx) {
                    Notifications.create()
                        .title("Error")
                        .text("Could not make copy of creature.")
                        .showError()
                }
            } catch (e: Exception) {
                LOGGER.log(
                    Level.SEVERE,
                    """
                    Could not make copy of creature.
                    ${Throwables.getStackTraceAsString(e)}
                    """.trimIndent()
                )

                GlobalScope.launch(Dispatchers.JavaFx) {
                    Notifications.create()
                        .title("Error")
                        .text("Could not make copy of creature.")
                        .showError()
                }
            }
        }

        /**
         * Publish the current creature XML file.
         *
         * @param device creature
         * @param deviceXMLFile creature XML file
         * @param commitMessage commit message
         */
        private fun publishCreature(
            device: MobileBase,
            deviceXMLFile: File,
            commitMessage: String
        ) {
            try {
                val git = ScriptingEngine.locateGit(deviceXMLFile)
                val remote = git.repository.config.getString("remote", "origin", "url")
                val relativePath = ScriptingEngine.findLocalPath(deviceXMLFile, git)

                // Push to existing gist
                ScriptingEngine.pushCodeToGit(
                    remote,
                    ScriptingEngine.getFullBranch(remote),
                    relativePath,
                    device.xml,
                    commitMessage
                )
            } catch (e: Exception) {
                LOGGER.severe(
                    """
                    Could not commit.
                    ${Throwables.getStackTraceAsString(e)}
                    """.trimIndent()
                )

                GlobalScope.launch(Dispatchers.JavaFx) {
                    Notifications.create()
                        .title("Commit error")
                        .text("Could not make commit.")
                        .showError()
                }
            }
        }

        private fun createLimbLayoutFxmlLoader(mobileBase: MobileBase, resourcePath: String) =
            FXMLLoader(
                CreatureEditorController::class.java.getResource(resourcePath),
                null,
                null,
                Callback {
                    BowlerBuilder.injector
                        .createChildInjector(LimbLayoutControllerModule(mobileBase))
                        .getInstance(it)
                }
            )
    }
}
