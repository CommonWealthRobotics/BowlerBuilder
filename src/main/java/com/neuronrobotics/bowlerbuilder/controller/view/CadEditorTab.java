package com.neuronrobotics.bowlerbuilder.controller.view;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;

/**
 * Tab used for editing CAD scripts.
 *
 * @param <T> controller type
 */
public abstract class CadEditorTab<T> extends ScriptEditorTab<T> {

  private final CadEngine cadEngine;

  public CadEditorTab(String title, ScriptEditorView scriptEditorView, CadEngine cadEngine) {
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
