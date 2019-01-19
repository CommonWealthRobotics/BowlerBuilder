/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.main.event.ApplicationClosingEvent
import com.neuronrobotics.bowlerkernel.hardware.Script
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
     */
    fun runScript(scriptText: String) {
        currentScript = scriptFactory.createScriptFromText(
            "groovy",
            scriptText
        )

        val result = currentScript.flatMap {
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

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onApplicationClosing(event: ApplicationClosingEvent) {
        currentScript.map { it.stopAndCleanUp() }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(AceEditorController::class.java.simpleName)
    }
}
