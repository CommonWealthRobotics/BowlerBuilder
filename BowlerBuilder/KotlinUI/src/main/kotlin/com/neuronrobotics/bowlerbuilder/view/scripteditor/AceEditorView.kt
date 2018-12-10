package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerGroovy
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import tornadofx.*

class AceEditorView : Fragment(), ScriptEditor {

    private val controller: AceEditorController by inject()
    private var webview: WebView by singleAssign()
    private var urlTextField: TextField by singleAssign()
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
        webview.engine.loadWorker.stateProperty().addListener { _, _, new ->
            if (new == Worker.State.SUCCEEDED) {
                urlTextField.text = params["git_url"] as String

                setText(
                    controller.getTextForGitResource(
                        params["git_url"] as String,
                        params["filename"] as String
                    )
                )

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
