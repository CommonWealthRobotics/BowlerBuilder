/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.view.gitmenu.PublishView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.geometry.Insets
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import tornadofx.*

class AceEditorView(
    private val editor: AceWebEditorView = find()
) : Fragment(), ScriptEditor by editor {

    private val controller: AceEditorController by inject()
    private var urlTextField: TextField by singleAssign()
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
                    find<PublishView>(
                        mapOf(
                            "git_url" to params["git_url"],
                            "filename" to params["filename"],
                            "file_content" to getFullText()
                        )
                    ).openModal()
                }
            }

            urlTextField = textfield {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
        }
    }

    init {
        runLater {
            urlTextField.text = params["git_url"] as String

            runAsync {
                val text = controller.getTextForGitResource(
                    params["git_url"] as String,
                    params["filename"] as String
                )

                engineInitializingLatch.await()

                runLater {
                    setText(text)
                    gotoLine(0)
                }
            }
        }
    }
}
