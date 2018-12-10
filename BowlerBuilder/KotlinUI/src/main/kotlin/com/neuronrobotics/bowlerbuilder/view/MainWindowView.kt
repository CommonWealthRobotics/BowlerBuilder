package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.consoletab.ConsoleTab
import com.neuronrobotics.bowlerbuilder.view.newtab.NewTabTab
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import javafx.geometry.Orientation
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.*
import java.util.Timer
import java.util.TimerTask
import java.util.logging.Level

class MainWindowView : View() {

    private var mainTabPane: TabPane by singleAssign()

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("Exit") {
                    action {
                        close()
                        beginForceQuit()
                    }
                }
            }
        }

        center = splitpane {
            orientation = Orientation.VERTICAL
            setDividerPositions(0.9)

            mainTabPane = tabpane {
                tabs += WebBrowserTab()
                tabs += NewTabTab().apply { isClosable = false }
            }

            tabpane {
                tabs += ConsoleTab().apply { isClosable = false }
            }
        }
    }

    fun addTab(tab: Tab) {
        mainTabPane.tabs.add(mainTabPane.tabs.size - 1, tab)
    }

    private fun beginForceQuit() {
        // Need to make sure the VM exits; sometimes a rouge thread is running
        // Wait 10 seconds before killing the VM
        val timer = Timer(true)
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    LOGGER.severe {
                        "Still alive for some reason. Printing threads and killing VM..."
                    }

                    val threads = Thread.getAllStackTraces().keys
                    val threadString = StringBuilder()
                    threads.forEach { item -> threadString.append(item).append("\n") }
                    LOGGER.log(Level.FINE, threadString.toString())

                    Runtime.getRuntime().exit(1) // Abnormal exit
                }
            },
            10000
        )
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(MainWindowView::class.java.simpleName)
    }
}
