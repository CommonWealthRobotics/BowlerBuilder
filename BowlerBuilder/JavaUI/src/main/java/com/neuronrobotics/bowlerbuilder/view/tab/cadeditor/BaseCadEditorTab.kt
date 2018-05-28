/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.tab.cadeditor

import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.controller.DefaultCadEditorTabController
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine
import com.neuronrobotics.bowlerbuilder.controller.module.DefaultCadEditorControllerModule
import com.neuronrobotics.bowlerbuilder.controller.module.DefaultCADModelViewerControllerModule
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView
import com.neuronrobotics.bowlerbuilder.view.tab.AbstractCadEditorTab
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.SplitPane

/**
 * An [AbstractCadEditorTab] that uses the default script editor layout and the default CAD layout.
 *
 * @param title the title of this tab
 * @param scriptEditorView the [ScriptEditorView] to use
 * @param cadEngine the [CadEngine] to use
 */
abstract class BaseCadEditorTab(
    title: String,
    scriptEditorView: ScriptEditorView,
    cadEngine: CadEngine
) :
        AbstractCadEditorTab<DefaultCadEditorTabController>(
                title,
                scriptEditorView,
                cadEngine) {

    private val defaultCadEditorController: DefaultCadEditorTabController
    private val pane: SplitPane

    init {
        val scriptEditorLoader = FXMLLoader(
                BaseCadEditorTab::class.java
                        .getResource(
                                "/com/neuronrobotics/bowlerbuilder/view/DefaultScriptEditor.fxml"),
                null,
                null,
                {
                    BowlerBuilder
                            .getInjector()
                            .createChildInjector(DefaultCadEditorControllerModule(scriptEditorView))
                            .getInstance(it)
                })
        val scriptEditor: Node = scriptEditorLoader.load()

        val cadViewerLoader = FXMLLoader(
                BaseCadEditorTab::class.java
                        .getResource(
                                "/com/neuronrobotics/bowlerbuilder/view/CADModelViewer.fxml"),
                null,
                null,
                {
                    BowlerBuilder
                            .getInjector()
                            .createChildInjector(DefaultCADModelViewerControllerModule())
                            .getInstance(it)
                })
        val cadViewer: Node = cadViewerLoader.load()

        pane = SplitPane(scriptEditor, cadViewer)
        defaultCadEditorController = DefaultCadEditorTabController(
                scriptEditorLoader.getController(),
                cadViewerLoader.getController()
        )

        // Set the JavaFX tab content
        content = pane
    }

    override val view: Node
        get() = pane

    override val controller: DefaultCadEditorTabController
        get() = defaultCadEditorController
}
