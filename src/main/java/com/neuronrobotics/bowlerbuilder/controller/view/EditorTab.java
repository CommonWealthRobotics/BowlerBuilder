package com.neuronrobotics.bowlerbuilder.controller.view;

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor;

/**
 * Tab used for editing scripts.
 *
 * @param <T> controller type
 */
public abstract class EditorTab<T> extends AbstractTab<T> {

  private final ScriptEditor scriptEditor;

  public EditorTab(String title, ScriptEditor scriptEditor) {
    super(title);
    this.scriptEditor = scriptEditor;
  }

  /**
   * Get the script editor.
   *
   * @return script editor
   */
  public ScriptEditor getScriptEditor() {
    return scriptEditor;
  }

}
