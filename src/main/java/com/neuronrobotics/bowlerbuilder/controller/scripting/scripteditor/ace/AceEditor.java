/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import javax.annotation.ParametersAreNonnullByDefault;

/** Editor for Cloud9 Ace. */
@ParametersAreNonnullByDefault
public final class AceEditor implements ScriptEditor {

  private static final Logger LOGGER = LoggerUtilities.getLogger(AceEditor.class.getSimpleName());

  private final WebEngineAdapter engine;

  public AceEditor(final WebEngineAdapter webEngine) {
    this.engine = webEngine;
    LOGGER.log(Level.FINEST, "engine: " + webEngine);
  }

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  @Override
  public void insertAtCursor(final String text) {
    runAfterEngine(
        () -> {
          final String escaped = escape(text);
          LOGGER.fine("Inserting: " + escaped);
          engine.executeScript("editor.insert(\"" + escaped + "\");");
        });
  }

  /**
   * Set the text in the editor, overwriting current content.
   *
   * @param text Text to insert
   */
  @Override
  public void setText(final String text) {
    runAfterEngine(
        () -> {
          final String escaped = escape(text);
          LOGGER.fine("Setting: " + escaped);
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
  @Override
  public void setFontSize(final int fontSize) {
    runAfterEngine(
        () ->
            engine.executeScript(
                "document.getElementById('editor').style.fontSize='" + fontSize + "px';"));
  }

  /**
   * Get the entire document text. Returns an empty string if the engine is not done loading.
   *
   * @return All text in the editor
   */
  @Override
  public String getText() {
    try {
      return returnAfterEngine(() -> (String) engine.executeScript("editor.getValue();"));
    } catch (final Exception e) {
      LOGGER.log(
          Level.SEVERE, "Could not get editor text.\n" + Throwables.getStackTraceAsString(e));
      return "";
    }
  }

  /**
   * Get the selected text. Returns an empty string if the engine is not done loading.
   *
   * @return The selected text
   */
  @Override
  public String getSelectedText() {
    try {
      return returnAfterEngine(
          () ->
              (String)
                  engine.executeScript("editor.session.getTextRange(editor.getSelectionRange());"));
    } catch (final Exception e) {
      LOGGER.log(
          Level.SEVERE, "Could not get selected text.\n" + Throwables.getStackTraceAsString(e));
      return "";
    }
  }

  /**
   * Move the cursor to a line.
   *
   * @param lineNumber Line number
   */
  @Override
  public void gotoLine(final int lineNumber) {
    runAfterEngine(() -> engine.executeScript("editor.gotoLine(" + lineNumber + ");"));
  }

  /**
   * Get the absolute cursor position as the number of characters in from the start of the text.
   * Returns 1 if the engine is not done loading.
   *
   * @return Cursor position
   */
  @Override
  public int getCursorPosition() {
    try {
      return returnAfterEngine(
          () ->
              (int)
                  engine.executeScript(
                      "editor.session.doc.positionToIndex(editor.selection.getCursor());"));
    } catch (final Exception e) {
      LOGGER.log(
          Level.SEVERE,
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
      engine
          .getLoadWorker()
          .stateProperty()
          .addListener(
              (observableValue, oldState, newState) -> {
                if (newState.equals(Worker.State.SUCCEEDED)) {
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
      throws ExecutionException, InterruptedException {
    final FutureTask<T> query = new FutureTask<>(callable);
    runAfterEngine(query);
    return query.get();
  }
}
