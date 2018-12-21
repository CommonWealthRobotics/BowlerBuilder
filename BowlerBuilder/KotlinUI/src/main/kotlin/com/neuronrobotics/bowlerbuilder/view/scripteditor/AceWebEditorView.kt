/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceWebEditorController
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import javafx.concurrent.Worker
import tornadofx.*

/**
 * An implementation of [ScriptEditor] which uses a webview to interface with the Cloud9 Ace
 * editor.
 */
class AceWebEditorView : Fragment(), ScriptEditor {

    private val controller = AceWebEditorController()
    val engineInitializingLatch = Latch(1)

    override val root = webview {
        id = "AceWebEditorView"
        engine.load(resources["/com/neuronrobotics/bowlerbuilder/web/ace.html"])
        engine.loadWorker.stateProperty().addListener { _, _, new ->
            if (new == Worker.State.SUCCEEDED) {
                engineInitializingLatch.countDown()
            }
        }
    }

    override fun insertAtCursor(text: String) {
        root.engine.executeScript("editor.insert(\"${controller.escape(text)}\");")
    }

    override fun setText(text: String) {
        root.engine.executeScript("editor.setValue(\"${controller.escape(text)}\");")
    }

    override fun getFullText() = root.engine.executeScript("editor.getValue();") as String

    override fun getSelectedText() = root.engine.executeScript(
        "editor.session.getTextRange(editor.getSelectionRange());"
    ) as String

    override fun gotoLine(lineNumber: Int) {
        root.engine.executeScript("editor.gotoLine($lineNumber);")
    }

    override fun getCursorPosition() = root.engine.executeScript(
        "editor.session.doc.positionToIndex(editor.selection.getCursor());"
    ) as Int
}
