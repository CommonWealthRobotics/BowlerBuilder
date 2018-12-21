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
import javax.inject.Inject

class AceEditorController
@Inject constructor(
    private val scriptRunner: ScriptRunner,
    private val scriptResultHandler: ScriptResultHandler
) : Controller() {

    /**
     * Runs a script by text using the injected [ScriptRunner].
     *
     * @param scriptText The full text of the script.
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
    fun getTextForGitResource(gitUrl: String, filename: String): String =
        ScriptingEngine.fileFromGit(gitUrl, filename).readText()

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(AceEditorController::class.java.simpleName)
    }
}
