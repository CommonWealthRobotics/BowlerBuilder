package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor;

/**
 * Interface to edit a text-based script.
 */
public interface ScriptEditor {

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  void insertAtCursor(String text);

  /**
   * Set the text in the editor, overwriting current content.
   *
   * @param text Text to insert
   */
  void setText(String text);

  /**
   * Set the font size for the editor.
   *
   * @param fontSize Font size
   */
  void setFontSize(int fontSize);

  /**
   * Get the entire document text.
   *
   * @return All text in the editor
   */
  String getText();

  /**
   * Get the selected text.
   *
   * @return The selected text
   */
  String getSelectedText();

  /**
   * Move the cursor to a line.
   *
   * @param lineNumber Line number
   */
  void gotoLine(int lineNumber);

  /**
   * Get the absolute cursor position as the number of characters in from the start of the text.
   *
   * @return Cursor position
   */
  int getCursorPosition();

}
