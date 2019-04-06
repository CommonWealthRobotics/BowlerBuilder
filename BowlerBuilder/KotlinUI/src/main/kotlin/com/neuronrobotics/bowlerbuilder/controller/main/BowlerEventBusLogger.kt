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
package com.neuronrobotics.bowlerbuilder.controller.main

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import org.greenrobot.eventbus.Logger
import java.util.logging.Level

class BowlerEventBusLogger(
    eventBusName: String
) : Logger {

    private val internalLogger = LoggerUtilities.getLogger(
        "${BowlerEventBusLogger::class.java.simpleName}$eventBusName"
    )

    override fun log(level: Level, msg: String) {
        internalLogger.log(level, msg)
    }

    override fun log(level: Level, msg: String, th: Throwable) {
        internalLogger.log(
            level,
            """
            |$msg
            |${Throwables.getStackTraceAsString(th)}
            """.trimMargin()
        )
    }
}
