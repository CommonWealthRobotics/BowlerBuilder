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

                thread {
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
        ): ThreadMonitoringButton {
            return ThreadMonitoringButton(primaryState, secondaryState, onRun)
        }
    }
}
