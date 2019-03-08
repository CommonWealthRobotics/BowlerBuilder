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
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.TextScriptRunner
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.model.WatchedFile
import com.neuronrobotics.bowlerbuilder.model.WatchedFileChange
import com.neuronrobotics.bowlerbuilder.view.gitmenu.PublishView
import com.neuronrobotics.bowlerbuilder.view.main.event.CloseTabByContentEvent
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.text.Text
import javafx.stage.Modality
import org.controlsfx.glyphfont.FontAwesome
import org.octogonapus.ktguava.collections.immutableListOf
import tornadofx.*
import java.io.File
import kotlin.concurrent.thread

/**
 * An editor which operates on a specific file and contains the controls to run the script and
 * push updates.
 */
@SuppressWarnings("SwallowedException")
class AceEditorView(
    private val editor: AceWebEditorView,
    private val scriptRunner: TextScriptRunner,
    private val gitUrl: String,
    private val file: File
) : Fragment(), ScriptEditor by editor, VisualScriptEditor {

    private val watchedFile = WatchedFile(file)
    private var urlTextField: TextField by singleAssign()
    val engineInitializingLatch
        get() = editor.engineInitializingLatch
    private var rootHoverProperty: ReadOnlyBooleanProperty by singleAssign()
    private var lastEditTime = Long.MAX_VALUE
    private var fileIsDirty = false
    private val threads: ImmutableList<Thread>

    override val root = borderpane {
        id = "AceEditorView"
        center = editor.root
        rootHoverProperty = hoverProperty()

        editor.root.setOnKeyPressed {
            // Keep track of time for autosave
            lastEditTime = System.nanoTime()
            fileIsDirty = true
        }

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT

            add(
                ThreadMonitoringButton(
                    "Run" to loadImageAsset("Run.png", FontAwesome.Glyph.PLAY),
                    "Stop" to loadImageAsset("Stop.png", FontAwesome.Glyph.STOP),
                    { scriptRunner.runScript(FxUtil.returnFX { getFullText() }, file.extension) },
                    { scriptRunner.stopScript() }
                )
            )

            button(
                "Publish",
                loadImageAsset("Publish.png", FontAwesome.Glyph.CLOUD_UPLOAD)
            ).action {
                writeContentToFile()
                PublishView(file).openModal()
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

        val fileWatcherThread = thread(isDaemon = true, name = "Editor FileWatcher") {
            while (!Thread.currentThread().isInterrupted) {
                if (rootHoverProperty.value) {
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
                                escapeClosesWindow = false,
                                block = true
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
                                escapeClosesWindow = false,
                                block = true
                            )
                        }

                        else -> Unit
                    }
                }

                try {
                    Thread.sleep(10)
                } catch (ex: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }

        val autosaveThread = thread(isDaemon = true, name = "Editor Autosave") {
            while (!Thread.currentThread().isInterrupted) {
                // If the user has not typed a key for a half second
                if (System.nanoTime() - lastEditTime >= 5e+8 && fileIsDirty) {
                    writeContentToFile()
                }

                try {
                    Thread.sleep(100)
                } catch (ex: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }

        threads = immutableListOf(fileWatcherThread, autosaveThread)
    }

    /**
     * Stops this editor's threads and calls [TextScriptRunner.stopScript].
     */
    fun stopThreads() {
        threads.forEach { it.interrupt() }
        scriptRunner.stopScript()
    }

    private fun writeContentToFile() {
        synchronized(watchedFile) {
            val text = FxUtil.returnFX { getFullText() }
            watchedFile.writeText(text)
            fileIsDirty = false
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
