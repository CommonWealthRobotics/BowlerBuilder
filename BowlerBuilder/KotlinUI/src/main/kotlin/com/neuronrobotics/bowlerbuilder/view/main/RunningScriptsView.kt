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

import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptRunningEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptStoppedEvent
import com.neuronrobotics.bowlerbuilder.view.util.getFontAwesomeGlyph
import com.neuronrobotics.bowlerkernel.hardware.Script
import javafx.geometry.Pos
import javafx.scene.control.CustomMenuItem
import org.controlsfx.glyphfont.FontAwesome
import org.greenrobot.eventbus.Subscribe
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktguava.collections.toImmutableMap
import tornadofx.*

private class MultipleScriptsView(
    private val scripts: ImmutableMap<Script, String>
) : View() {

    override val root = hbox {
        text("${scripts.size} scripts running") {
            contextmenu {
                scripts.forEach { script, name ->
                    this += CustomMenuItem().apply {
                        content = hbox {
                            spacing = 5.0
                            alignment = Pos.CENTER_LEFT

                            text(name)
                            button(
                                graphic = getFontAwesomeGlyph(FontAwesome.Glyph.TIMES_CIRCLE)
                            ).setOnAction {
                                runAsync { script.stopAndCleanUp() }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays which scripts are currently running. Send a [ScriptRunningEvent] when a script starts
 * running and send a [ScriptStoppedEvent] when a script stops running.
 */
class RunningScriptsView : Fragment() {

    private val scripts = mutableMapOf<Script, String>()

    override val root = hbox {
        this += MultipleScriptsView(emptyImmutableMap())
    }

    init {
        MainWindowController.mainUIEventBus.register(this)
    }

    private fun ScriptRunningEvent.nonEmptyDisplayName() =
        if (displayName.isEmpty())
            script::class.java.simpleName
        else
            displayName

    @Subscribe
    fun onScriptRunningEvent(event: ScriptRunningEvent) {
        scripts[event.script] = event.nonEmptyDisplayName()
        fixScriptView()
    }

    @Subscribe
    fun onScriptStoppedEvent(event: ScriptStoppedEvent) {
        scripts.remove(event.script)
        fixScriptView()
    }

    private fun fixScriptView() {
        runLater {
            // Refresh the context menu items
            root.children.remove(root.children.last())
            root.children.add(MultipleScriptsView(scripts.toImmutableMap()).root)
        }
    }
}
