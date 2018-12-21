/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceWebViewCommandMapper
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import javafx.concurrent.Worker
import tornadofx.*

/**
 * An implementation of [ScriptEditor] which uses a webview to interface with the Cloud9 Ace
 * editor.
 */
class AceWebEditorView : Fragment(), ScriptEditor {

    private val mapper = AceWebViewCommandMapper()
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
        root.engine.executeScript(mapper.insertAtCursor(text))
    }

    override fun setText(text: String) {
        root.engine.executeScript(mapper.setText(text))
    }

    override fun getFullText() = root.engine.executeScript(mapper.getFullText()) as String

    override fun getSelectedText() = root.engine.executeScript(mapper.getSelectedText()) as String

    override fun gotoLine(lineNumber: Int) {
        root.engine.executeScript(mapper.gotoLine(lineNumber))
    }

    override fun getCursorPosition() = root.engine.executeScript(mapper.getCursorPosition()) as Int
}
