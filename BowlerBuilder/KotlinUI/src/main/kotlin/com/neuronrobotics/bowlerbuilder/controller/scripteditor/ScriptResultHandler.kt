/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.control.Tab
import tornadofx.*

/**
 * A utility class to interpret the result from running a script.
 */
class ScriptResultHandler {

    /**
     * Handles the [result] from running a script.
     */
    fun handleResult(result: Any?) {
        if (result != null) {
            when (result) {
                is MobileBaseLoader -> handleMobileBase(result.base)
                is MobileBase -> handleMobileBase(result)
                is CSG -> handleCSG(result)
                is Tab -> handleTab(result)
            }
        }
    }

    private fun handleMobileBase(base: MobileBase) {
        TODO("not implemented")
    }

    private fun handleCSG(result: CSG) {
        TODO("not implemented")
    }

    private fun handleTab(result: Tab) {
        find<MainWindowView>().addTab(result)
    }
}
