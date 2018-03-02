package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;
import javafx.scene.Node;
import javax.annotation.Nonnull;

/**
 * Tab used for editing scripts.
 *
 * @param <T> controller type
 */
public abstract class AbstractScriptEditorTab<T> extends AbstractTab<T> {

  private final ScriptEditorView scriptEditorView;

  public AbstractScriptEditorTab(@Nonnull final String title,
      @Nonnull final ScriptEditorView scriptEditorView) {
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
