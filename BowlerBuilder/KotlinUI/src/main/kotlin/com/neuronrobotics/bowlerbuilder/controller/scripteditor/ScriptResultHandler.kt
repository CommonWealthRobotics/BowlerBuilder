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

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.SetCadObjectsToCurrentTabEvent
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.control.Tab
import org.octogonapus.guavautil.collections.immutableSetOf
import org.octogonapus.guavautil.collections.toImmutableSet

/**
 * A utility class to interpret the result from running a script.
 */
class ScriptResultHandler {

    /**
     * Handles the [result] from running a script.
     */
    fun handleResult(result: Any?) {
        when (result) {
            is CSG -> handleCsg(result)
            is Iterable<*> -> handleIterable(result)
            is Tab -> handleTab(result)
        }
    }

    private fun handleIterable(result: Iterable<*>) {
        @Suppress("UNCHECKED_CAST")
        when (result.first()) {
            is CSG -> handleCsg(result as Iterable<CSG>)
        }
    }

    private fun handleCsg(result: CSG) = handleCsg(immutableSetOf(result))

    private fun handleCsg(result: Iterable<CSG>) =
        MainWindowController.mainUIEventBus.post(
            SetCadObjectsToCurrentTabEvent(
                result.toImmutableSet()
            )
        )

    private fun handleTab(result: Tab) = MainWindowController.mainUIEventBus.post(
        AddTabEvent(result)
    )
}
