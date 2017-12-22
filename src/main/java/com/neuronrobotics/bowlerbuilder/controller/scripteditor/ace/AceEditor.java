package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Worker;

/**
 * Editor for Cloud9 Ace.
 */
public final class AceEditor implements ScriptEditor {

  private static final Logger logger = LoggerUtilities.getLogger(AceEditor.class.getSimpleName());

  private final WebEngineAdapter engine;

  public AceEditor(WebEngineAdapter webEngine) {
    this.engine = webEngine;
    logger.log(Level.FINEST, "engine: " + webEngine);
  }

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  public void insertAtCursor(String text) {
    runAfterEngine(() -> {
      String escaped = text;
      escaped = escaped.replace("\"", "\\\"");
      escaped = escaped.replace("'", "\\'");
      escaped = escaped.replace(System.getProperty("line.separator"), "\\n");
      escaped = escaped.replace("\n", "\\n");
      escaped = escaped.replace("\r", "\\n");
      logger.log(Level.FINE,
          "Inserting: " + escaped);
      engine.executeScript("editor.insert(\"" + escaped + "\");");
    });
  }

  /**
   * Set the font size for the editor.
   *
   * @param fontSize Font size
   */
  public void setFontSize(int fontSize) {
    runAfterEngine(() ->
        engine.executeScript(
            "document.getElementById('editor').style.fontSize='"
                + fontSize
                + "px';"));
  }

  /**
   * Get the entire document text. Returns an empty string if the engine is not done loading.
   *
   * @return All text in the editor
   */
  public String getText() {
    if (checkEngine()) {
      return (String) engine.executeScript("editor.getValue();");
    }

    return "";
  }

  /**
   * Get the selected text. Returns an empty string if the engine is not done loading.
   *
   * @return The selected text
   */
  public String getSelectedText() {
    if (checkEngine()) {
      return (String)
          engine.executeScript("editor.session.getTextRange(editor.getSelectionRange());");
    }

    return "";
  }

  /**
   * Move the cursor to a line.
   *
   * @param lineNumber Line number
   */
  public void gotoLine(int lineNumber) {
    runAfterEngine(() -> engine.executeScript("editor.gotoLine(" + lineNumber + ");"));
  }

  /**
   * Get the absolute cursor position as the number of characters in from the start of the text.
   * Returns 1 if the engine is not done loading.
   *
   * @return Cursor position
   */
  public int getCursorPosition() {
    if (checkEngine()) {
      return (int) engine.executeScript(
          "editor.session.doc.positionToIndex(editor.selection.getCursor());");
    }

    return 1;
  }

  /**
   * Return if the engine is done loading, and is safe to execute scripts on.
   *
   * @return whether the engine is done loading
   */
  private boolean checkEngine() {
    return engine.getLoadWorker().getState().equals(Worker.State.SUCCEEDED);
  }

  /**
   * Run the runnable after the engine is done loading.
   *
   * @param runnable runnable to run
   */
  private void runAfterEngine(Runnable runnable) {
    if (checkEngine()) {
      runnable.run();
    } else {
      engine.getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
        if (t1.equals(Worker.State.SUCCEEDED)) {
          runnable.run();
        }
      });
    }
  }
}
