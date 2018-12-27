/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.view.gitmenu.PublishView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.geometry.Insets
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*
import java.io.File
import javax.inject.Inject

/**
 * An editor which operates on a specific file and contains the controls to run the script and
 * push updates.
 */
class AceEditorView
@Inject constructor(
    private val editor: AceWebEditorView,
    private val controller: AceEditorController,
    private val gitUrl: String,
    private val file: File
) : Fragment(), ScriptEditor by editor, VisualScriptEditor {

    private var urlTextField: TextField by singleAssign()
    val engineInitializingLatch
        get() = editor.engineInitializingLatch

    override val root = borderpane {
        id = "AceEditorView"
        center = editor.root

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true

            add(
                ThreadMonitoringButton.create(
                    "Run" to loadImageAsset("Run.png", FontAwesome.Glyph.PLAY),
                    "Stop" to loadImageAsset("Stop.png", FontAwesome.Glyph.STOP)
                ) {
                    controller.runScript(FxUtil.returnFX { getFullText() })
                }
            )

            button("Publish", loadImageAsset("Publish.png", FontAwesome.Glyph.CLOUD_UPLOAD)) {
                action {
                    PublishView.create(gitUrl, file, getFullText()).openModal()
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
            urlTextField.text = gitUrl

            runAsync {
                val text = controller.getTextForGitResource(gitUrl, file.name)

                engineInitializingLatch.await()

                text.map {
                    runLater {
                        setText(it)
                        gotoLine(0)
                    }
                }
            }
        }
    }

    companion object {
        fun create(url: String, file: File) = AceEditorView(
            AceWebEditorView(),
            getInstanceOf<AceEditorController>(),
            url,
            file
        )

        fun create(gistFile: GistFile) = AceEditorView(
            AceWebEditorView(),
            getInstanceOf<AceEditorController>(),
            gistFile.gist.gitUrl,
            gistFile.file
        )
    }
}
