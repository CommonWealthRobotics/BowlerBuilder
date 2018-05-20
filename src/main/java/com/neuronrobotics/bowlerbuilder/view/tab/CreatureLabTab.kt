/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.tab

import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.AceCreatureLabController
import com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine.BowlerCadEngine
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditorView
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import java.util.function.Supplier

/**
 * An [AbstractCadEditorTab] that uses an [AceCreatureLabController] with a [BowlerCadEngine].
 *
 * @param title the title of this tab
 */
class CreatureLabTab(title: String) :
        AbstractCadEditorTab<AceCreatureLabController>(
                title,
                BowlerBuilder.getInjector().getInstance(AceEditorView::class.java),
                BowlerBuilder.getInjector().getInstance(BowlerCadEngine::class.java)) {

    private val creatureLabController: AceCreatureLabController
    private val pane: SplitPane

    init {
        val creatureEditorLoader = FXMLLoader(
                CreatureLabTab::class.java
                        .getResource(
                                "/com/neuronrobotics/bowlerbuilder/view/CreatureEditor.fxml"),
                null,
                null,
                {
                    BowlerBuilder
                            .getInjector()
                            .getInstance(it)
                })
        val creatureEditor: Node = creatureEditorLoader.load()

        val cadViewerLoader = FXMLLoader(
                CreatureLabTab::class.java
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

        // Contains script editors for the creature's files
        val tabPane = TabPane()

        pane = SplitPane(tabPane, creatureEditor, cadViewer)
        pane.setDividerPositions(0.2, 0.7)

        creatureLabController = AceCreatureLabController(
                tabPane,
                Supplier {
                    FXMLLoader(
                            CreatureLabTab::class.java
                                    .getResource(
                                            "/com/neuronrobotics/bowlerbuilder/view/" +
                                                    "AceScriptEditor.fxml"),
                            null,
                            null,
                            {
                                BowlerBuilder
                                        .getInjector()
                                        .createChildInjector(AceCadEditorControllerModule(
                                                BowlerBuilder
                                                        .getInjector()
                                                        .getInstance(AceEditorView::class.java)
                                        ))
                                        .getInstance(it)
                            })
                },
                cadViewerLoader.getController(),
                creatureEditorLoader.getController()
        )

        content = pane
    }

    override val scriptEditorView: ScriptEditorView
        get() = throw UnsupportedOperationException("CreatureLabTab does not have just one " +
                "script editor and therefore does not support getting the ScriptEditorView.")

    override val view: Node
        get() = pane

    override val controller: AceCreatureLabController
        get() = creatureLabController

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(CreatureLabTab::class.java.simpleName)
    }
}
