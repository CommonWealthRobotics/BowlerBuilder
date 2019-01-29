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
package com.neuronrobotics.bowlerbuilder.view.util

import javafx.scene.Node
import javafx.scene.control.Button
import tornadofx.*
import kotlin.concurrent.thread

/**
 * A button that runs [onRun] on another thread and monitors for that thread to stop on another
 * thread. Starts at the [primaryState] and transitions to the [secondaryState] when [onRun] is
 * running. Switches back to [primaryState] when [onRun] stops or is interrupted with
 * [Thread.interrupt].
 */
@SuppressWarnings("SwallowedException")
class ThreadMonitoringButton
private constructor(
    primaryState: Pair<String, Node>,
    secondaryState: Pair<String, Node>,
    val onRun: () -> Unit
) : Button(primaryState.first, primaryState.second) {
    private var managedThread: Thread? = null

    init {
        setOnAction {
            if (text == primaryState.first) {
                text = secondaryState.first
                graphic = secondaryState.second

                managedThread = thread { onRun() }

                thread(isDaemon = true) {
                    try {
                        managedThread?.join()
                    } catch (ex: InterruptedException) {
                        // This is probably the user clicking stop
                    }

                    managedThread = null
                    runLater {
                        text = primaryState.first
                        graphic = primaryState.second
                    }
                }
            } else {
                managedThread?.interrupt()
                managedThread = null
                runLater {
                    text = primaryState.first
                    graphic = primaryState.second
                }
            }
        }
    }

    companion object {
        fun create(
            primaryState: Pair<String, Node>,
            secondaryState: Pair<String, Node>,
            onRun: () -> Unit
        ) = ThreadMonitoringButton(primaryState, secondaryState, onRun)
    }
}
