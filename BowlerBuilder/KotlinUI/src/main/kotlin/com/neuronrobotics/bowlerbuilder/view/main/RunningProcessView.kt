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
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptRunningEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.ScriptStoppedEvent
import javafx.geometry.Pos
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktguava.collections.toImmutableMap
import tornadofx.*

private class MultipleProcessView<T>(
    private val processes: ImmutableMap<T, String>,
    private val processName: String,
    private inline val extraConfiguration: HBox.(T) -> Unit
) : View() {

    override val root = hbox {
        text("${processes.size} $processName running") {
            contextmenu {
                processes.forEach { process, name ->
                    this += CustomMenuItem().apply {
                        content = hbox {
                            spacing = 5.0
                            alignment = Pos.CENTER_LEFT

                            text(name)
                            extraConfiguration(process)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays which processes are currently running. Send a [ScriptRunningEvent] when a process starts
 * running and send a [ScriptStoppedEvent] when a process stops running.
 *
 * @param processName The type of process this view displays (e.g., "scripts", "processes").
 * @param extraConfiguration Any extra configuration added to a processes' [MenuItem].
 */
class RunningProcessView<T>(
    private val processName: String,
    private inline val extraConfiguration: HBox.(T) -> Unit
) : Fragment() {

    private val processes = mutableMapOf<T, String>()

    override val root = hbox {
        this += MultipleProcessView(emptyImmutableMap(), processName, extraConfiguration)
    }

    /**
     * Adds a process.
     *
     * @param process An object which represents the process.
     * @param name The display name.
     */
    fun addProcess(process: T, name: String) {
        processes[process] = name
        fixScriptView()
    }

    /**
     * Removes a process.
     *
     * @param process An object which represents the process.
     */
    fun removeProcess(process: T) {
        processes.remove(process)
        fixScriptView()
    }

    private fun fixScriptView() {
        runLater {
            // Refresh the context menu items
            root.children.remove(root.children.last())
            root.children.add(
                MultipleProcessView(
                    processes.toImmutableMap(),
                    processName,
                    extraConfiguration
                ).root
            )
        }
    }
}
