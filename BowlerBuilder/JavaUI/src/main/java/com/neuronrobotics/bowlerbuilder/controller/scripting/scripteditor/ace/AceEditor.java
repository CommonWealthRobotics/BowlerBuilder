/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace.WebEngineAdapter;
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.view.util.WebEngineUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
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
    WebEngineUtil.runAfterEngine(
        engine.getLoadWorker(),
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
    WebEngineUtil.runAfterEngine(
        engine.getLoadWorker(),
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
   * Get the entire document text. Returns an empty string if the engine is not done loading.
   *
   * @return All text in the editor
   */
  @Nonnull
  @Override
  public String getFullText() {
    try {
      return WebEngineUtil.returnAfterEngine(
          engine.getLoadWorker(), () -> (String) engine.executeScript("editor.getValue();"));
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
  @Nonnull
  @Override
  public String getSelectedText() {
    try {
      return WebEngineUtil.returnAfterEngine(
          engine.getLoadWorker(),
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
    WebEngineUtil.runAfterEngine(
        engine.getLoadWorker(), () -> engine.executeScript("editor.gotoLine(" + lineNumber + ");"));
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
      return WebEngineUtil.returnAfterEngine(
          engine.getLoadWorker(),
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
}
