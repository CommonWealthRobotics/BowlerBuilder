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
