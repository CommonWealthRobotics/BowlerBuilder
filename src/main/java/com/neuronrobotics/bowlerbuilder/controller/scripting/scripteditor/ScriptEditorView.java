package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor;

import javafx.scene.Node;

/**
 * Interface to interact with a {@link ScriptEditor} through some {@link Node}.
 */
public interface ScriptEditorView {

  /**
   * Get the view for editing the script.
   *
   * @return editor view
   */
  Node getView();

  /**
   * Get the {@link ScriptEditor} this view interacts with.
   *
   * @return the script editor
   */
  ScriptEditor getScriptEditor();

}
