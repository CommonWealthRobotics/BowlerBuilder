/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerGroovy
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.web.WebView
import tornadofx.*

class AceScratchpadView : Fragment(), ScriptEditor {

    private val controller: AceEditorController by inject()
    private val scriptEditorFactory: ScriptEditorFactory by di()
    private var webview: WebView by singleAssign()
    val engineInitializingLatch = Latch(1)

    override val root = borderpane {
        webview = webview {
            engine.load(resources["/com/neuronrobotics/bowlerbuilder/web/ace.html"])
        }

        center = webview

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true

            this += ThreadMonitoringButton.create(
                "Run" to AssetFactory.loadIcon("Run.png"),
                "Stop" to AssetFactory.loadIcon("Stop.png")
            ) {
                controller.scriptRunner.runScript(
                    FxUtil.returnFX { getFullText() },
                    null,
                    BowlerGroovy.SHELL_TYPE
                )
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

    init {
        webview.engine.loadWorker.stateProperty().addListener { _, _, new ->
            if (new == Worker.State.SUCCEEDED) {
                engineInitializingLatch.countDown()
            }
        }
    }

    override fun insertAtCursor(text: String) {
        webview.engine.executeScript("editor.insert(\"${controller.escape(text)}\");")
    }

    override fun setText(text: String) {
        webview.engine.executeScript("editor.setValue(\"${controller.escape(text)}\");")
    }

    override fun getFullText(): String {
        return webview.engine.executeScript("editor.getValue();") as String
    }

    override fun getSelectedText(): String {
        return webview.engine.executeScript(
            "editor.session.getTextRange(editor.getSelectionRange());"
        ) as String
    }

    override fun gotoLine(lineNumber: Int) {
        webview.engine.executeScript("editor.gotoLine($lineNumber);")
    }

    override fun getCursorPosition(): Int {
        return webview.engine.executeScript(
            "editor.session.doc.positionToIndex(editor.selection.getCursor());"
        ) as Int
    }
}
