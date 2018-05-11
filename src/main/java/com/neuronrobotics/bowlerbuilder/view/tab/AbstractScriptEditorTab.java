/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;
import javafx.scene.Node;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Tab used for editing scripts.
 *
 * @param <T> controller type
 */
@ParametersAreNonnullByDefault
public abstract class AbstractScriptEditorTab<T> extends AbstractTab<T> {

  private final ScriptEditorView scriptEditorView;

  public AbstractScriptEditorTab(final String title, final ScriptEditorView scriptEditorView) {
    super(title);
    this.scriptEditorView = scriptEditorView;
  }

  /**
   * Get the script editor view.
   *
   * @return script editor view
   */
  public ScriptEditorView getScriptEditorView() {
    return scriptEditorView;
  }

  @Override
  public Node getView() {
    return scriptEditorView.getView();
  }
}
