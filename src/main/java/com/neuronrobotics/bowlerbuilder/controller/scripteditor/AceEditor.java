package com.neuronrobotics.bowlerbuilder.controller.scripteditor;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebEngine;

public final class AceEditor implements ScriptEditor {

  private final Logger logger = Logger.getLogger(AceEditor.class.getSimpleName());

  private final WebEngine engine;

  public AceEditor(WebEngine engine) {
    this.engine = engine;

    LoggerUtilities.setupLogger(logger);
  }

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  public void insertAtCursor(String text) {
    String escaped = text;
    escaped = escaped.replace("\"", "\\\"");
    escaped = escaped.replace("'", "\\'");
    escaped = escaped.replace(System.getProperty("line.separator"), "\\n");
    escaped = escaped.replace("\n", "\\n");
    escaped = escaped.replace("\r", "\\n");
    logger.log(Level.FINE,
        "Inserting: " + escaped);
    engine.executeScript("editor.insert(\"" + escaped + "\");");
  }

  /**
   * Set the font size for the editor.
   *
   * @param fontSize Font size
   */
  public void setFontSize(int fontSize) {
    engine.executeScript("document.getElementById('editor').style.fontSize='" + fontSize + "px';");
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
