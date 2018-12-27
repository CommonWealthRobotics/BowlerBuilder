/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import arrow.core.Try
import arrow.core.flatMap
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.util.filesInRepo
import com.neuronrobotics.bowlerkernel.scripting.TextScriptFactory
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import tornadofx.*
import javax.inject.Inject

class AceEditorController
@Inject constructor(
    private val scriptFactory: TextScriptFactory,
    private val scriptResultHandler: ScriptResultHandler
) : Controller() {

    /**
     * Runs a script by text using the injected [scriptFactory].
     *
     * @param scriptText The full text of the script.
     */
    fun runScript(scriptText: String) {
        val result = scriptFactory.createScriptFromText(
            "groovy",
            scriptText
        ).flatMap {
            it.addToInjector(MainWindowController.mainModule())
            it.runScript(emptyImmutableList())
        }

        result.bimap(
            {
                LOGGER.warning {
                    """
                    |Error running script:
                    |$it
                    """.trimMargin()
                }
            },
            {
                scriptResultHandler.handleResult(it)
            }
        )
    }

    /**
     * Get the content of a file from git.
     */
    fun getTextForGitResource(gitUrl: String, filename: String): Try<String> {
        return filesInRepo(
            getInstanceOf<MainWindowController>().credentials,
            gitUrl
        ).map {
            it.first { it.name == filename }.readText()
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(AceEditorController::class.java.simpleName)
    }
}
