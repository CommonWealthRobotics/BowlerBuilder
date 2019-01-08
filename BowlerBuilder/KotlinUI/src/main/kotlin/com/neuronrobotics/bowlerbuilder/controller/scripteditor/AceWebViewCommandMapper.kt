/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

class AceWebViewCommandMapper {

    fun insertAtCursor(text: String) = "editor.insert(\"${escape(text)}\");"

    fun setText(text: String) = "editor.setValue(\"${escape(text)}\");"

    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getFullText() = "editor.getValue();"

    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getSelectedText() = "editor.session.getTextRange(editor.getSelectionRange());"

    fun gotoLine(lineNumber: Int) = "editor.gotoLine($lineNumber);"

    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getCursorPosition() = "editor.session.doc.positionToIndex(editor.selection.getCursor());"

    /**
     * Escape text so it gets inserted properly.
     *
     * @param text Text to escape
     * @return Escaped version
     */
    fun escape(text: String): String {
        var escaped = text
        escaped = escaped.replace("\"", "\\\"")
        escaped = escaped.replace("'", "\\'")
        escaped = escaped.replace(System.getProperty("line.separator"), "\\n")
        escaped = escaped.replace("\n", "\\n")
        escaped = escaped.replace("\r", "\\n")
        return escaped
    }
}
