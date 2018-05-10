/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor;

import javax.annotation.Nonnull;

/** Interface to edit a text-based script. */
public interface ScriptEditor {

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  void insertAtCursor(@Nonnull String text);

  /**
   * Set the text in the editor, overwriting current content.
   *
   * @param text Text to insert
   */
  void setText(@Nonnull String text);

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
