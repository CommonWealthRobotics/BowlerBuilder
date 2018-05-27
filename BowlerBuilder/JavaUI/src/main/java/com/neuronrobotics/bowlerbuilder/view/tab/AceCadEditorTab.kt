/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.tab

import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.controller.AceCadEditorTabController
import com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine.BowlerCadEngine
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace.AceEditorView
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.SplitPane

/**
 * An [AbstractCadEditorTab] that uses an [AceEditorView] and a [BowlerCadEngine].
 */
class AceCadEditorTab(title: String) :
        AbstractCadEditorTab<AceCadEditorTabController>(
                title,
                BowlerBuilder.getInjector().getInstance(AceEditorView::class.java),
                BowlerBuilder.getInjector().getInstance(BowlerCadEngine::class.java)) {

    private val cadEditorController: AceCadEditorTabController
    private val pane: SplitPane

    init {
        val scriptEditorLoader = FXMLLoader(
                AceCadEditorTab::class.java
                        .getResource(
                                "/com/neuronrobotics/bowlerbuilder/view/AceScriptEditor.fxml"),
                null,
                null,
                {
                    BowlerBuilder
                            .getInjector()
                            .createChildInjector(AceCadEditorControllerModule(scriptEditorView))
                            .getInstance(it)
                })
        val scriptEditor: Node = scriptEditorLoader.load()

        val cadViewerLoader = FXMLLoader(
                AceCadEditorTab::class.java
                        .getResource(
                                "/com/neuronrobotics/bowlerbuilder/view/CADModelViewer.fxml"),
                null,
                null,
                {
                    BowlerBuilder
                            .getInjector()
                            .createChildInjector(CADModelViewerControllerModule())
                            .getInstance(it)
                })
        val cadViewer: Node = cadViewerLoader.load()

        pane = SplitPane(scriptEditor, cadViewer)
        cadEditorController = AceCadEditorTabController(
                scriptEditorLoader.getController(),
                cadViewerLoader.getController()
        )

        // Set the JavaFX tab content
        content = pane
    }

    override val view: Node
        get() = pane

    override val controller: AceCadEditorTabController
        get() = cadEditorController
}
