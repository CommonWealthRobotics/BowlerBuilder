/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import tornadofx.*

class AceEditorController : Controller() {

    val scriptRunner: ScriptRunner by di()

    fun getTextForGitResource(gitUrl: String, filename: String): String {
        return ScriptingEngine.fileFromGit(gitUrl, filename).readText()
    }

    /**
     * Escape text so it gets inserted properly.
     *
     * @param text Text to escape
     * @return Escaped version
     */
    fun escape(text: String): String {
        var escaped = text
        escaped = escaped.replace("\"", "\\\"")
        escaped = escaped.replace("'", "\\'")
        escaped = escaped.replace(System.getProperty("line.separator"), "\\n")
        escaped = escaped.replace("\n", "\\n")
        escaped = escaped.replace("\r", "\\n")
        return escaped
    }
}
