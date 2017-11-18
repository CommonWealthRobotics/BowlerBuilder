package com.neuronrobotics.bowlerbuilder.controller

import javafx.scene.web.WebEngine

class AceInterface(private val engine: WebEngine) {

    /**
     * Get the entire document text.
     *
     * @return All text in the editor
     */
    val text: String
        get() = engine.executeScript("editor.getValue();") as String

    /**
     * Get the selected text.
     *
     * @return The selected text
     */
    val selectedText: String
        get() = engine.executeScript("editor.session.getTextRange(editor.getSelectionRange());") as String

    /**
     * Get the absolute cursor position as the number of characters in from the start of the text.
     *
     * @return Cursor position
     */
    val cursorPosition: Int
        get() = engine.executeScript("editor.session.doc.positionToIndex(editor.selection.getCursor());") as Int

    /**
     * Insert text at the cursor position.
     *
     * @param text Text to insert
     */
    fun insertAtCursor(text: String) {
        engine.executeScript("editor.insert(\"$text\");")
    }

    /**
     * Set the font size for the editor.
     *
     * @param fontSize Font size
     */
    fun setFontSize(fontSize: Int) {
        engine.executeScript("document.getElementById('editor').style.fontSize='" + fontSize + "px';")
        //TODO: save font size preference
    }

    /**
     * Move the cursor to a line.
     *
     * @param lineNumber Line number
     */
    fun gotoLine(lineNumber: Int) {
        engine.executeScript("editor.gotoLine($lineNumber);")
    }

}
