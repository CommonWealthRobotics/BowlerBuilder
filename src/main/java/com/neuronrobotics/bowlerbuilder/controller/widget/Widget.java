/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.widget;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.io.Serializable;
import java.util.ArrayList;

public class Widget implements Serializable {

  private final String gitSource;
  private final String displayName;

  public Widget(String gitSource, String displayName) {
    this.gitSource = gitSource;
    this.displayName = displayName;
  }

  /**
   * Clone and run the gist code for this Widget.
   */
  public void run() throws Exception {
    ScriptingEngine.gitScriptRun(gitSource, "main.groovy", new ArrayList<>());
  }

  @Override
  public String toString() {
    return displayName;
  }

  public String getGitSource() {
    return gitSource;
  }

  public String getDisplayName() {
    return displayName;
  }

}
