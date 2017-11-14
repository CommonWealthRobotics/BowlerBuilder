package com.neuronrobotics.bowlerbuilder.controller;

import javafx.scene.web.WebEngine;

public final class AceInterface {

  private final WebEngine engine;

  public AceInterface(WebEngine engine) {
    this.engine = engine;
  }

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  public void insertAtCursor(String text) {
    engine.executeScript("editor.insert(\"" + text + "\");");
  }

  /**
   * Set the font size for the editor.
   *
   * @param fontSize Font size
   */
  public void setFontSize(int fontSize) {
    engine.executeScript("document.getElementById('editor').style.fontSize='" + fontSize + "px';");
    //TODO: save font size preference
  }

  /**
   * Get the entire document text.
   *
   * @return All text in the editor
   */
  public String getText() {
    return (String) engine.executeScript("editor.getValue();");
  }

  /**
   * Get the selected text.
   *
   * @return The selected text
   */
  public String getSelectedText() {
    return (String)
        engine.executeScript("editor.session.getTextRange(editor.getSelectionRange());");
  }

  /**
   * Move the cursor to a line.
   *
   * @param lineNumber Line number
   */
  public void gotoLine(int lineNumber) {
    engine.executeScript("editor.gotoLine(" + lineNumber + ");");
  }

  /**
   * Get the absolute cursor position as the number of characters in from the start of the text.
   *
   * @return Cursor position
   */
  public int getCursorPosition() {
    return (int)
        engine.executeScript("editor.session.doc.positionToIndex(editor.selection.getCursor());");
  }

}
