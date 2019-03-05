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
import arrow.core.Try
import arrow.core.left
import arrow.core.recover
import arrow.core.right
import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.main.event.ApplicationClosingEvent
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage
import com.neuronrobotics.bowlerkernel.scripting.factory.TextScriptFactory
import org.greenrobot.eventbus.Subscribe
import org.octogonapus.guavautil.collections.emptyImmutableList
import tornadofx.*
import javax.inject.Inject

class AceEditorController
@Inject constructor(
    private val scriptFactory: TextScriptFactory,
    private val scriptResultHandler: ScriptResultHandler
) : Controller() {

    private var currentScript: Either<String, Script> = "Not initialized.".left()

    init {
        MainWindowController.mainUIEventBus.register(this)
    }

    /**
     * Runs a script by text using the injected [scriptFactory].
     *
     * @param scriptText The full text of the script.
     * @param language A string representing the script language.
     */
    fun runScript(scriptText: String, language: String) {
        currentScript = scriptFactory.createScriptFromText(
            language,
            scriptText
        )

        currentScript.fold(
            {
                LOGGER.warning {
                    """
                    |Error creating script:
                    |$it
                    """.trimMargin()
                }
            },
            { runAndHandleScript(it) }
        )
    }

    /**
     * Runs a script by text using the injected [scriptFactory].
     *
     * @param scriptText The full text of the script.
     * @param language The language of the script.
     */
    fun runScript(scriptText: String, language: ScriptLanguage) {
        currentScript = scriptFactory.createScriptFromText(
            language,
            scriptText
        ).right()

        currentScript.fold(
            {
                LOGGER.warning {
                    """
                    |Error creating script:
                    |$it
                    """.trimMargin()
                }
            },
            { runAndHandleScript(it) }
        )
    }

    private fun runAndHandleScript(script: Script) {
        script.addToInjector(MainWindowController.mainModule())

        val result = Try {
            script.runScript(emptyImmutableList())
        }

        result.map {
            scriptResultHandler.handleResult(it)
        }.recover {
            LOGGER.warning {
                """
                |Error running script:
                |${Throwables.getStackTraceAsString(it)}
                """.trimMargin()
            }
        }
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onApplicationClosing(event: ApplicationClosingEvent) {
        currentScript.map { it.stopAndCleanUp() }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(AceEditorController::class.java.simpleName)
    }
}
