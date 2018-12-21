/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.view.CloseTabByContentEvent
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.gitmenu.PublishNewGistView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.geometry.Insets
import org.jlleitschuh.guice.key
import tornadofx.*
import javax.inject.Inject

/**
 * An editor which operates entirely in memory and contains the controls to run the script and
 * create a new gist with the editor contents.
 */
class AceScratchpadView
@Inject constructor(
    private val editor: AceWebEditorView,
    private val controller: AceEditorController,
    private val cadScriptEditorFactory: CadScriptEditorFactory
) : Fragment(), ScriptEditor by editor, VisualScriptEditor {

    val engineInitializingLatch
        get() = editor.engineInitializingLatch

    @SuppressWarnings("LabeledExpression")
    override val root = borderpane {
        id = "AceScratchpadView"
        center = editor.root

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true

            this += ThreadMonitoringButton.create(
                "Run" to AssetFactory.loadIcon("Run.png"),
                "Stop" to AssetFactory.loadIcon("Stop.png")
            ) {
                controller.runScript(FxUtil.returnFX { getFullText() })
            }

            button("Publish", AssetFactory.loadIcon("Publish.png")) {
                action {
                    val view = PublishNewGistView.create(getFullText()).apply {
                        openModal(block = true)
                    }

                    if (view.publishSuccessful) {
                        runAsync {
                            cadScriptEditorFactory.createAndOpenScriptEditor(
                                GistFile.create(view.gitUrl, view.gistFilename)
                            )
                        }

                        MainWindowView.mainUIEventBus.post(CloseTabByContentEvent(this@borderpane))
                    }
                }
            }
        }
    }

    companion object {
        fun create() = AceScratchpadView(
            AceWebEditorView(),
            MainWindowView.injector.getInstance(key<AceEditorController>()),
            MainWindowView.injector.getInstance(key<CadScriptEditorFactory>())
        )
    }
}