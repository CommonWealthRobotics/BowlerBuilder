/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import java.lang.Exception

interface ScriptRunner {

    /**
     * Run a script and return the result.
     *
     * @param script code
     * @param arguments arguments
     * @param languageName language name
     * @return result
     * @throws Exception a script could throw an exception
     */
    @Throws(Exception::class)
    fun runScript(script: String, arguments: ArrayList<Any>?, languageName: String): Any

    /**
     * Get whether the script is currently compiling.
     *
     * @return whether the script it compiling
     */
    fun isScriptCompiling(): Boolean

    /**
     * Get whether the script is currently compiling.
     *
     * @return whether the script it compiling
     */
    fun scriptCompilingProperty(): ReadOnlyBooleanProperty

    /**
     * Get whether the script is currently running.
     *
     * @return whether the script is currently running
     */
    fun isScriptRunning(): Boolean

    /**
     * Get whether the script is currently running.
     *
     * @return whether the script is currently running
     */
    fun scriptRunningProperty(): ReadOnlyBooleanProperty

    /**
     * Return value of the script.
     *
     * @return return value from the script
     */
    fun resultProperty(): ReadOnlyObjectProperty<Any>
}
