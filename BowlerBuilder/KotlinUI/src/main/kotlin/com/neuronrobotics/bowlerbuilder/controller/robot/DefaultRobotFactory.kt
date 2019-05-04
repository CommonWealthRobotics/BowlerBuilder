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
package com.neuronrobotics.bowlerbuilder.controller.robot

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerbuilder.model.robot.Robot
import com.neuronrobotics.bowlerbuilder.model.robot.RobotData
import com.neuronrobotics.bowlercad.cadgenerator.CadGenerator
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBaseFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.getInstanceFromGit
import javax.inject.Inject

class DefaultRobotFactory
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val kinematicBaseFactory: KinematicBaseFactory
) : RobotFactory {

    override fun createRobot(robotData: RobotData): Either<RobotCreationError, Robot> {
        val kinematicBase = kinematicBaseFactory.create(robotData.kinematicBase).fold(
            { return it.left() }, { it }
        )

        val cadGenerator = scriptFactory.getInstanceFromGit<CadGenerator>(
            robotData.cadGenerator
        ).getOrHandle { return it.left() }

        return Robot(kinematicBase, cadGenerator).right()
    }
}
