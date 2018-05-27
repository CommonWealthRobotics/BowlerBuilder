/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.scripting.scripteditor

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
