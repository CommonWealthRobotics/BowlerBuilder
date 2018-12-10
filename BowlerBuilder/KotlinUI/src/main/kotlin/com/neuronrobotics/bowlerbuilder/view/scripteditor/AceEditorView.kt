package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorController
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerGroovy
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.geometry.Insets
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import tornadofx.*

class AceEditorView : Fragment(), ScriptEditor {

    private val controller: AceEditorController by inject()
    private var webview: WebView by singleAssign()
    private var urlTextField: TextField by singleAssign()

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
                controller.runScript(
                    FxUtil.returnFX { getFullText() },
                    null,
                    BowlerGroovy.SHELL_TYPE
                )
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

            setText(
                controller.getTextForGitResource(
                    params["git_url"] as String,
                    params["filename"] as String
                )
            )
        }
    }

    override fun insertAtCursor(text: String) {
        TODO("not implemented")
    }

    override fun setText(text: String) {
        webview.engine.executeScript("editor.setValue(\"${controller.escape(text)}\");")
    }

    override fun getFullText(): String {
        return webview.engine.executeScript("editor.getValue();") as String
    }

    override fun getSelectedText(): String {
        TODO("not implemented")
    }

    override fun gotoLine(lineNumber: Int) {
        TODO("not implemented")
    }

    override fun getCursorPosition(): Int {
        TODO("not implemented")
    }
}
