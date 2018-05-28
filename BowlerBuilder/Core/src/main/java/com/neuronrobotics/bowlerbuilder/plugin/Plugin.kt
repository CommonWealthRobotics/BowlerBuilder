/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.plugin

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import java.util.ArrayList

data class Plugin(val gitSource: String, val displayName: String) {

    /**
     * Clone and run the gist code for this Plugin.
     *
     * @throws Exception running the plugin script could throw an exception
     */
    @Throws(Exception::class)
    fun run(): Any? =
            ScriptingEngine.gitScriptRun(gitSource, "main.groovy", ArrayList())
}
