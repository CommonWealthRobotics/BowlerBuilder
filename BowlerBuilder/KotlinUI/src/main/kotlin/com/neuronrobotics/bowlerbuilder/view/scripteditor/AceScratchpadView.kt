/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.geometry.Insets
import tornadofx.*

class AceScratchpadView(
    private val editor: AceWebEditorView = find()
) : Fragment(), ScriptEditor by editor {

    private val controller: AceEditorController by inject()
    private val scriptEditorFactory: ScriptEditorFactory by di()
    val engineInitializingLatch
        get() = editor.engineInitializingLatch

    override val root = borderpane {
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
                    val view = find<PublishNewGistView>(
                        mapOf("file_content" to getFullText())
                    ).apply {
                        openModal(block = true)
                    }

                    if (view.publishSuccessful) {
                        runAsync {
                            scriptEditorFactory.createAndOpenScriptEditor(
                                view.gitUrl,
                                view.gistFilename
                            )
                        }

                        find<MainWindowView>().closeTabByContent(this@borderpane)
                    }
                }
            }
        }
    }
}
