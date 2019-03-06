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
import com.neuronrobotics.bowlerbuilder.model.robot.Robot
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.SetCadObjectsToCurrentTabEvent
import com.neuronrobotics.bowlercad.cadgenerator.CadGenerator
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.control.Tab
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.plus
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableSet

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
            is Collection<*> -> handleCollection(result)
            is Tab -> handleTab(result)
            is KinematicBase -> handleKinematicBase(result)
            is Robot -> handleRobot(result)
        }
    }

    private fun handleCollection(result: Collection<*>) {
        if (result.size == 2) {
            val resultList = result.toImmutableList()
            if (resultList[0] is KinematicBase && resultList[1] is CadGenerator) {
                handleKinematicBaseWithCadGenerator(
                    resultList[0] as KinematicBase,
                    resultList[1] as CadGenerator
                )
            } else if (resultList[0] is CadGenerator && resultList[1] is KinematicBase) {
                handleKinematicBaseWithCadGenerator(
                    resultList[1] as KinematicBase,
                    resultList[0] as CadGenerator
                )
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            when (result.first()) {
                is CSG -> handleCsg(result as Collection<CSG>)
            }
        }
    }

    private fun handleCsg(csg: CSG) = handleCsg(immutableSetOf(csg))

    private fun handleCsg(csgs: Iterable<CSG>) =
        MainWindowController.mainUIEventBus.post(
            SetCadObjectsToCurrentTabEvent(
                csgs.toImmutableSet()
            )
        )

    private fun handleTab(tab: Tab) = MainWindowController.mainUIEventBus.post(
        AddTabEvent(tab)
    )

    private fun handleKinematicBase(base: KinematicBase) {
    }

    private fun handleKinematicBaseWithCadGenerator(base: KinematicBase, cadGen: CadGenerator) {
        val bodyCad = cadGen.generateBody(base)
        val limbsCad = cadGen.generateLimbs(base)

        MainWindowController.mainUIEventBus.post(
            SetCadObjectsToCurrentTabEvent(
                immutableSetOf(bodyCad) + limbsCad.values().flatten().toImmutableSet()
            )
        )
    }

    private fun handleRobot(robot: Robot) =
        handleKinematicBaseWithCadGenerator(robot.kinematicBase, robot.cadGenerator)
}
