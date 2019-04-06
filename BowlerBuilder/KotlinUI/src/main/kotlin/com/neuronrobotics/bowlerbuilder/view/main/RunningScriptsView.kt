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
package com.neuronrobotics.bowlerbuilder.view.main

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptRunningEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptStoppedEvent
import com.neuronrobotics.bowlerbuilder.view.util.getFontAwesomeGlyph
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.controlsfx.glyphfont.FontAwesome
import org.greenrobot.eventbus.Subscribe
import tornadofx.*

/**
 * Displays which scripts are currently running. Send a [ScriptRunningEvent] when a scripts starts
 * running and send a [ScriptStoppedEvent] when a scripts stops running.
 */
class RunningScriptsView : Fragment() {

    private val processView = RunningProcessView<Script>(
        "scripts"
    ) { script ->
        button(
            graphic = getFontAwesomeGlyph(FontAwesome.Glyph.TIMES_CIRCLE)
        ).setOnAction {
            runAsync { script.stopAndCleanUp() }
        }
    }

    override val root = processView.root

    init {
        MainWindowController.mainUIEventBus.register(this)
    }

    @Subscribe
    fun onScriptRunningEvent(event: ScriptRunningEvent) {
        processView.addProcess(event.script, event.nonEmptyDisplayName())
    }

    @Subscribe
    fun onScriptStoppedEvent(event: ScriptStoppedEvent) {
        processView.removeProcess(event.script)
    }

    private fun ScriptRunningEvent.nonEmptyDisplayName() =
        if (displayName.isEmpty())
            script::class.java.simpleName
        else
            displayName
}
