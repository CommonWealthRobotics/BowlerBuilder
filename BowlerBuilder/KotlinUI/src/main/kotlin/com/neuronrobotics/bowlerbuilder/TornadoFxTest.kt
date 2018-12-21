/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import tornadofx.*

fun main(args: Array<String>) {
    launch<TornadoFxTest>(args)
}

class TornadoFxTest : App(MainWindowView::class) {

    init {
        runLater {
            // Log uncaught exceptions on the FX thread
            Thread.currentThread().setUncaughtExceptionHandler { _, exception ->
                LOGGER.severe(Throwables.getStackTraceAsString(exception))
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            LOGGER.severe(Throwables.getStackTraceAsString(exception))
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(TornadoFxTest::class.java.simpleName)
    }
}
