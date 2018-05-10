/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner;

import java.util.ArrayList;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javax.annotation.Nonnull;

/** Interface for compiling and running a script. */
public interface ScriptRunner {

  /**
   * Run a script and return the result.
   *
   * @param script code
   * @param arguments arguments
   * @param languageName language name
   * @return result
   * @throws Exception a script could throw an exception
   */
  Object runScript(
      @Nonnull String script, ArrayList<Object> arguments, @Nonnull String languageName) // NOPMD
      throws Exception; // NOPMD

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

  /**
   * Return value of the script.
   *
   * @return return value from the script
   */
  ReadOnlyObjectProperty<Object> resultProperty();
}
