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

import arrow.core.Try
import arrow.core.recover
import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptRunningEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptStoppedEvent
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.util.logging.Logger

/**
 * Runs a script and handles its result.
 *
 * @param script The script to run.
 * @param scriptResultHandler The handler to give the script result to.
 * @param logger The logger to log to.
 * @param displayName The name to display for the script when it is running.
 */
internal fun runAndHandleScript(
    script: Script,
    scriptResultHandler: ScriptResultHandler,
    logger: Logger,
    displayName: String = ""
) {
    script.addToInjector(Script.getDefaultModules())
    script.addToInjector(MainWindowController.mainModule())

    MainWindowController.mainUIEventBus.post(ScriptRunningEvent(script, displayName))

    val result = Try {
        script.runScript(emptyImmutableList())
    }

    result.map {
        it.fold(
            {
                """
                |Error running script:
                |$it
                """.trimMargin()
            },
            { scriptResultHandler.handleResult(it) }
        )

        MainWindowController.mainUIEventBus.post(ScriptStoppedEvent(script))
    }.recover {
        logger.warning {
            """
            |Exception running script:
            |${Throwables.getStackTraceAsString(it)}
            """.trimMargin()
        }

        MainWindowController.mainUIEventBus.post(ScriptStoppedEvent(script))
    }
}
