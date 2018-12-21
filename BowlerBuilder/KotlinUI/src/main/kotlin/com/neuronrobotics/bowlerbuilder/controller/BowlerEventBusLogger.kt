/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
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
