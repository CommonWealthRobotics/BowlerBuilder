package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;

/**
 * Tab used for editing CAD scripts.
 *
 * @param <T> controller type
 */
public abstract class AbstractCadEditorTab<T> extends AbstractScriptEditorTab<T> {

  private final CadEngine cadEngine;

  public AbstractCadEditorTab(final String title, final ScriptEditorView scriptEditorView,
      final CadEngine cadEngine) {
    super(title, scriptEditorView);
    this.cadEngine = cadEngine;
  }

  /**
   * Get the CAD engine.
   *
   * @return CAD engine
   */
  public CadEngine getCadEngine() {
    return cadEngine;
  }

}
