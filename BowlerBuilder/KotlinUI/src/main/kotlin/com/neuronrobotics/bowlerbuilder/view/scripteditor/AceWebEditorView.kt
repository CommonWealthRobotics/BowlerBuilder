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

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceWebViewCommandMapper
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor
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
