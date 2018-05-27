/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.scripting.scriptrunner

import com.neuronrobotics.bowlerbuilder.util.Verified
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty

interface ScriptRunner {

    /**
     * Run a script and return the result.
     *
     * @param script code
     * @param arguments arguments
     * @param languageName language name
     * @return result
     */
    fun runScript(script: String, arguments: ArrayList<Any>?, languageName: String): Verified<Exception, Any?>

    /**
     * The result of running the script.
     */
    fun getResult(): Verified<Exception, Any?>

    /**
     * The result of running the script.
     */
    fun resultProperty(): ReadOnlyObjectProperty<Verified<Exception, Any?>>

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
}
