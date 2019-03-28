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

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptRunningEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptStoppedEvent
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.greenrobot.eventbus.Subscribe
import org.octogonapus.ktguava.collections.toImmutableList
import tornadofx.*

private class OneScriptView(
    private val scriptName: String
) : View() {

    override val root = hbox {
        text(scriptName)
    }
}

private class MultipleScriptsView(
    private val scripts: ImmutableList<Script>
) : View() {

    override val root = hbox {
        text("${scripts.size} running")
        progressbar()
    }
}

class RunningScriptsView : Fragment() {

    private var runningScriptView: View = OneScriptView("")
    private val scripts = mutableListOf<Script>()

    override val root = hbox {
        text("Running Script: ")
        this += runningScriptView
    }

    init {
        MainWindowController.mainUIEventBus.register(this)
    }

    @Subscribe
    fun onScriptRunningEvent(event: ScriptRunningEvent) {
        scripts.add(event.script)
        fixScriptView()
    }

    @Subscribe
    fun onScriptStoppedEvent(event: ScriptStoppedEvent) {
        scripts.remove(event.script)
        fixScriptView()
    }

    private fun fixScriptView() {
        runLater {
            when {
                scripts.isEmpty() -> {
                    root.children.remove(root.children.last())
                    root.children.add(OneScriptView("").root)
                }

                scripts.size == 1 -> {
                    root.children.remove(root.children.last())
                    root.children.add(OneScriptView(scripts.first()::class.java.simpleName).root)
                }

                else -> {
                    root.children.remove(root.children.last())
                    root.children.add(MultipleScriptsView(scripts.toImmutableList()).root)
                }
            }
        }
    }
}
