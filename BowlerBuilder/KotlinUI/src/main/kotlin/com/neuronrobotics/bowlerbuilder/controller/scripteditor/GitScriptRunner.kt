/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import arrow.core.Either
import arrow.core.left
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.main.event.ApplicationClosingEvent
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class GitScriptRunner
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val scriptResultHandler: ScriptResultHandler
) {

    private var currentScript: Either<String, Script> = "Not initialized.".left()

    init {
        MainWindowController.mainUIEventBus.register(this)
    }

    /**
     * Runs a script by text using the injected [scriptFactory].
     *
     * @param gitFile The Git file.
     */
    fun runScript(gitFile: GitFile) {
        stopScript()
        currentScript = scriptFactory.createScriptFromGit(gitFile)

        currentScript.fold(
            {
                LOGGER.warning {
                    """
                    |Error creating script:
                    |$it
                    """.trimMargin()
                }
            },
            { runAndHandleScript(it, scriptResultHandler, LOGGER, gitFile.filename) }
        )
    }

    /**
     * Calls [Script.stopAndCleanUp] on the [currentScript], if there is one.
     */
    fun stopScript() {
        currentScript.map { it.stopAndCleanUp() }
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onApplicationClosing(event: ApplicationClosingEvent) {
        currentScript.map { it.stopAndCleanUp() }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(GitScriptRunner::class.java.simpleName)
    }
}
