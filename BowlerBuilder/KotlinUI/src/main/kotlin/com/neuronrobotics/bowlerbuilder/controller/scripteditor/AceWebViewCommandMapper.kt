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
