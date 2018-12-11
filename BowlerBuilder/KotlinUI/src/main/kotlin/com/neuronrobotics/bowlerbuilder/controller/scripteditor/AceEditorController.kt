/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerGroovy
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import tornadofx.*

class AceEditorController : Controller() {

    private val scriptRunner: ScriptRunner by di()
    private val scriptResultHandler: ScriptResultHandler by di()

    /**
     * Runs a script by text using the injected [ScriptRunner].
     */
    fun runScript(scriptText: String) {
        scriptRunner.runScript(
            scriptText,
            null,
            BowlerGroovy.SHELL_TYPE
        ).handle(
            {
                scriptResultHandler.handleResult(it)
            },
            {
                LOGGER.warning {
                    """
                    |Error running script:
                    |${Throwables.getStackTraceAsString(it)}
                    """.trimMargin()
                }
            }
        )
    }

    /**
     * Get the content of a file from git.
     */
    fun getTextForGitResource(gitUrl: String, filename: String): String {
        return ScriptingEngine.fileFromGit(gitUrl, filename).readText()
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(AceEditorController::class.java.simpleName)
    }
}
