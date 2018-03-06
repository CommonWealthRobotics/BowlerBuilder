/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;

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
