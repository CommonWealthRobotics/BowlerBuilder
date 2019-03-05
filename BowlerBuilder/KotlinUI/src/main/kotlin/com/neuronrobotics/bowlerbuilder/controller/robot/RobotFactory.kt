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
import com.neuronrobotics.bowlerbuilder.model.robot.Robot
import com.neuronrobotics.bowlerbuilder.model.robot.RobotData

interface RobotFactory {

    /**
     * Creates a new robot from the [robotData].
     *
     * @param robotData The data to construct the robot from.
     */
    fun createRobot(robotData: RobotData): Either<RobotCreationError, Robot>
}
