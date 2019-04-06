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
package com.neuronrobotics.bowlerbuilder

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.main.event.CadViewExplodedEvent
import tornadofx.*

fun main(args: Array<String>) {
    launch<BowlerBuilder>(args)
}

class BowlerBuilder : App(MainWindowView::class) {

    init {
        runLater {
            // Log uncaught exceptions on the FX thread
            Thread.currentThread().setUncaughtExceptionHandler { _, exception ->
                when (exception) {
                    is NullPointerException,
                    is ArrayIndexOutOfBoundsException ->
                        @SuppressWarnings("ComplexCondition")
                        if (exception.stackTrace == null ||
                            exception.stackTrace.isEmpty() ||
                            exception.stackTrace.map { it.methodName }.any {
                                it == "updateCachedBounds" || it == "synchronizeSceneNodes"
                            }
                        ) {
                            LOGGER.info("JavaFX Exploded")
                            MainWindowController.mainUIEventBus.post(CadViewExplodedEvent)
                        } else {
                            LOGGER.severe(Throwables.getStackTraceAsString(exception))
                        }

                    else -> LOGGER.severe(Throwables.getStackTraceAsString(exception))
                }
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            LOGGER.severe(Throwables.getStackTraceAsString(exception))
        }
    }

    override fun stop() {
        super.stop()
        MainWindowController.beginForceQuit()
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(BowlerBuilder::class.java.simpleName)
    }
}
