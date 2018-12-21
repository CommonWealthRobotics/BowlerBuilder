/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.SetCadObjectsToCurrentTabEvent
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.kinematicschef.util.immutableSetOf
import com.neuronrobotics.kinematicschef.util.toImmutableSet
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.control.Tab

/**
 * A utility class to interpret the result from running a script.
 */
class ScriptResultHandler {

    /**
     * Handles the [result] from running a script.
     */
    fun handleResult(result: Any?) {
        when (result) {
            is MobileBaseLoader -> handleMobileBase(result.base)
            is MobileBase -> handleMobileBase(result)
            is CSG -> handleCsg(result)
            is Iterable<*> -> handleIterable(result)
            is Tab -> handleTab(result)
        }
    }

    // return new Cube(5).toCSG()
    private fun handleIterable(result: Iterable<*>) {
        when (result.first()) {
            is CSG -> handleCsg(result as Iterable<CSG>)
        }
    }

    private fun handleMobileBase(base: MobileBase) {
        TODO("not implemented")
    }

    private fun handleCsg(result: CSG) = handleCsg(immutableSetOf(result))

    private fun handleCsg(result: Iterable<CSG>) =
        MainWindowView.mainUIEventBus.post(
            SetCadObjectsToCurrentTabEvent(
                result.toImmutableSet()
            )
        )

    private fun handleTab(result: Tab) = MainWindowView.mainUIEventBus.post(
        AddTabEvent(result)
    )
}
