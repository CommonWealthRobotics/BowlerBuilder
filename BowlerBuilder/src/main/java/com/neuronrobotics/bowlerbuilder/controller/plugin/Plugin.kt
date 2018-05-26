/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.plugin

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import java.io.Serializable
import java.util.ArrayList

class Plugin(val gitSource: String, val displayName: String) : Serializable {

    /**
     * Clone and run the gist code for this Plugin.
     *
     * @throws Exception running the plugin script could throw an exception
     */
    @Throws(Exception::class)
    fun run(): Any? =
            ScriptingEngine.gitScriptRun(gitSource, "main.groovy", ArrayList())

    override fun toString(): String =
            displayName

    companion object {
        private const val serialVersionUID = -4350419926001196348L
    }
}
