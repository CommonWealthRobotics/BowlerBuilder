/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor;

import javafx.scene.Node;
import javax.annotation.ParametersAreNonnullByDefault;

/** Interface to interact with a {@link ScriptEditor} through some {@link Node}. */
@ParametersAreNonnullByDefault
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
