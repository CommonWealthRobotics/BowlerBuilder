/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.model.WatchedFile
import com.neuronrobotics.bowlerbuilder.model.WatchedFileChange
import com.neuronrobotics.bowlerbuilder.view.gitmenu.PublishView
import com.neuronrobotics.bowlerbuilder.view.main.event.CloseTabByContentEvent
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.text.Text
import javafx.stage.Modality
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*
import java.io.File
import javax.inject.Inject
import kotlin.concurrent.thread

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

    private val watchedFile = WatchedFile(file)
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
            alignment = Pos.CENTER_LEFT

            add(
                ThreadMonitoringButton.create(
                    "Run" to loadImageAsset("Run.png", FontAwesome.Glyph.PLAY),
                    "Stop" to loadImageAsset("Stop.png", FontAwesome.Glyph.STOP)
                ) {
                    controller.runScript(FxUtil.returnFX { getFullText() })
                }
            )

            button(
                "Publish",
                loadImageAsset("Publish.png", FontAwesome.Glyph.CLOUD_UPLOAD)
            ).action {
                PublishView.create(file).openModal()
            }

            urlTextField = textfield {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }

                textProperty().addListener { _, _, newValue ->
                    val textWidth = Text(newValue).layoutBounds.width
                    val paddingWidth = padding.left + padding.right
                    val fontPadding = font.size * 1.5
                    maxWidth = textWidth + paddingWidth + fontPadding
                }
            }

            button("Copy URL").action {
                clipboard.putString(urlTextField.text)
            }

            button("Copy Path").action {
                clipboard.putString(file.absolutePath)
            }
        }
    }

    init {
        runLater {
            urlTextField.text = gitUrl
        }

        thread(isDaemon = true) {
            val text = file.readText()

            engineInitializingLatch.await()
            runLater {
                setText(text)
                gotoLine(0)
            }
        }

        thread(isDaemon = true) {
            while (true) {
                when (watchedFile.wasFileChangedSinceLastCheck()) {
                    WatchedFileChange.MODIFIED -> runLater {
                        FileModifiedOnDiskView(
                            {
                                val text = getFullText()
                                runAsync { watchedFile.writeText(text) }
                            },
                            {
                                // TODO: Put cursor back in the same position
                                setText(file.readText())
                            }
                        ).openModal(
                            modality = Modality.WINDOW_MODAL,
                            escapeClosesWindow = false
                        )
                    }

                    WatchedFileChange.DELETED -> runLater {
                        FileDeletedOnDiskView(
                            {
                                val text = getFullText()
                                runAsync { watchedFile.writeText(text) }
                            },
                            {
                                MainWindowController.mainUIEventBus.post(
                                    CloseTabByContentEvent(root)
                                )
                            }
                        ).openModal(
                            modality = Modality.WINDOW_MODAL,
                            escapeClosesWindow = false
                        )
                    }

                    else -> Unit
                }

                Thread.sleep(10)
            }
        }
    }

    companion object {
        fun create(url: String, file: File) = AceEditorView(
            AceWebEditorView(),
            getInstanceOf(),
            url,
            file
        )
    }
}
