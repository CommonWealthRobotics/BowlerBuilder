package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner;

import java.util.ArrayList;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * Interface for compiling and running a script.
 */
public interface ScriptRunner {

  /**
   * Run a script and return the result.
   *
   * @param script code
   * @param arguments arguments
   * @param languageName language name
   * @return result
   */
  Object runScript(String script, ArrayList<Object> arguments, String languageName)
      throws Exception;

  /**
   * Get whether the script is currently compiling.
   *
   * @return whether the script it compiling
   */
  boolean isScriptCompiling();

  /**
   * Get whether the script is currently compiling.
   *
   * @return whether the script it compiling
   */
  ReadOnlyBooleanProperty scriptCompilingProperty();

  /**
   * Get whether the script is currently running.
   *
   * @return whether the script is currently running
   */
  boolean isScriptRunning();

  /**
   * Get whether the script is currently running.
   *
   * @return whether the script is currently running
   */
  ReadOnlyBooleanProperty scriptRunningProperty();

}
