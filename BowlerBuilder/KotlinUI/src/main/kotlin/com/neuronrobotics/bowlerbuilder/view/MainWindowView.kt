package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.view.consoletab.ConsoleTab
import com.neuronrobotics.bowlerbuilder.view.newtab.NewTabTab
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import javafx.geometry.Orientation
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.*

class MainWindowView : View() {

    private var mainTabPane: TabPane by singleAssign()

    override val root = borderpane {
        top = menubar {
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
}
