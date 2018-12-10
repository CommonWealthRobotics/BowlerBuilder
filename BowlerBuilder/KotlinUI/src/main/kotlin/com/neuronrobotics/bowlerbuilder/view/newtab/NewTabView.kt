package com.neuronrobotics.bowlerbuilder.view.newtab

import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import javafx.geometry.Insets
import tornadofx.*

class NewTabView : View() {

    override val root = flowpane {
        padding = Insets(5.0)

        mapOf(
            "Web Browser" to { WebBrowserTab() }
        ).forEach { (tabName, tabGenerator) ->
            button(tabName) {
                action {
                    find<MainWindowView>().addTab(tabGenerator())
                }
            }
        }
    }
}
