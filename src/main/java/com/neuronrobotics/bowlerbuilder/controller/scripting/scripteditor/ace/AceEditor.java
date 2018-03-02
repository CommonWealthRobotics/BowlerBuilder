package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Worker;

/**
 * Editor for Cloud9 Ace.
 */
public final class AceEditor implements ScriptEditor {

  private static final Logger logger = LoggerUtilities.getLogger(AceEditor.class.getSimpleName());

  private final WebEngineAdapter engine;

  public AceEditor(final WebEngineAdapter webEngine) {
    this.engine = webEngine;
    logger.log(Level.FINEST, "engine: " + webEngine);
  }

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  public void insertAtCursor(final String text) {
    runAfterEngine(() -> {
      final String escaped = escape(text);
      logger.fine("Inserting: " + escaped);
      engine.executeScript("editor.insert(\"" + escaped + "\");");
    });
  }

  /**
   * Set the text in the editor, overwriting current content.
   *
   * @param text Text to insert
   */
  public void setText(final String text) {
    runAfterEngine(() -> {
      final String escaped = escape(text);
      logger.fine("Setting: " + escaped);
      engine.executeScript("editor.setValue(\"" + escaped + "\");");
    });
  }

  /**
   * Escape text so it gets inserted properly.
   *
   * @param text Text to escape
   * @return Escaped version
   */
  private String escape(final String text) {
    String escaped = text;
    escaped = escaped.replace("\"", "\\\"");
    escaped = escaped.replace("'", "\\'");
    escaped = escaped.replace(System.getProperty("line.separator"), "\\n");
    escaped = escaped.replace("\n", "\\n");
    escaped = escaped.replace("\r", "\\n");
    return escaped;
  }

  /**
   * Set the font size for the editor.
   *
   * @param fontSize Font size
   */
  public void setFontSize(final int fontSize) {
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
    try {
      return returnAfterEngine(() -> (String) engine.executeScript("editor.getValue();"));
    } catch (final Exception e) {
      logger.log(Level.SEVERE,
          "Could not get editor text.\n" + Throwables.getStackTraceAsString(e));
      return "";
    }
  }

  /**
   * Get the selected text. Returns an empty string if the engine is not done loading.
   *
   * @return The selected text
   */
  public String getSelectedText() {
    try {
      return returnAfterEngine(() -> (String)
          engine.executeScript("editor.session.getTextRange(editor.getSelectionRange());"));
    } catch (final Exception e) {
      logger.log(Level.SEVERE,
          "Could not get selected text.\n" + Throwables.getStackTraceAsString(e));
      return "";
    }
  }

  /**
   * Move the cursor to a line.
   *
   * @param lineNumber Line number
   */
  public void gotoLine(final int lineNumber) {
    runAfterEngine(() -> engine.executeScript("editor.gotoLine(" + lineNumber + ");"));
  }

  /**
   * Get the absolute cursor position as the number of characters in from the start of the text.
   * Returns 1 if the engine is not done loading.
   *
   * @return Cursor position
   */
  public int getCursorPosition() {
    try {
      return returnAfterEngine(() -> (int) engine.executeScript(
          "editor.session.doc.positionToIndex(editor.selection.getCursor());"));
    } catch (final Exception e) {
      logger.log(Level.SEVERE,
          "Could not get editor cursor position.\n" + Throwables.getStackTraceAsString(e));
      return 1;
    }
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
  private void runAfterEngine(final Runnable runnable) {
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

  /**
   * Run the callable after the engine is done loading and return the result.
   *
   * @param callable callable to run
   * @param <T> return type of callable
   * @return callable return value
   * @throws ExecutionException when running callable
   * @throws InterruptedException when running callable
   */
  private <T> T returnAfterEngine(final Callable<T> callable)
      throws Exception {
    final FutureTask<T> query = new FutureTask<>(callable);
    runAfterEngine(query);
    return query.get();
  }

}
