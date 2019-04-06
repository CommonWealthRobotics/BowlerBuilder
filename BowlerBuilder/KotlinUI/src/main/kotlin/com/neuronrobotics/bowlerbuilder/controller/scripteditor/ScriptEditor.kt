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

interface ScriptEditor {

    /**
     * Insert text at the cursor position.
     *
     * @param text Text to insert
     */
    fun insertAtCursor(text: String)

    /**
     * Set the text in the editor, overwriting current content.
     *
     * @param text Text to insert
     */
    fun setText(text: String)

    /**
     * Get the entire document text.
     *
     * @return All text in the editor
     */
    fun getFullText(): String

    /**
     * Get the selected text.
     *
     * @return The selected text
     */
    fun getSelectedText(): String

    /**
     * Move the cursor to a line.
     *
     * @param lineNumber Line number
     */
    fun gotoLine(lineNumber: Int)

    /**
     * Get the absolute cursor position as the number of characters in from the start of the text.
     *
     * @return Cursor position
     */
    fun getCursorPosition(): Int
}
