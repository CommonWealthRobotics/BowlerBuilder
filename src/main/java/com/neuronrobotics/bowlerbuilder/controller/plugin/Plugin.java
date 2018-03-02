package com.neuronrobotics.bowlerbuilder.controller.plugin;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class Plugin implements Serializable {

  private final String gitSource;
  private final String displayName;

  public Plugin(@Nonnull final String gitSource, @Nonnull final String displayName) {
    this.gitSource = gitSource;
    this.displayName = displayName;
  }

  /**
   * Clone and run the gist code for this Plugin.
   */
  public void run() throws Exception { //NOPMD
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
