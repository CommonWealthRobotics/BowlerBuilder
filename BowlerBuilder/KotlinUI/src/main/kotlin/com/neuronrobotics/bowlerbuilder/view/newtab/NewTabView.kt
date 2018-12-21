/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.newtab

import com.neuronrobotics.bowlerbuilder.view.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import javafx.geometry.Insets
import tornadofx.*

/**
 * A view which holds buttons to create various new tabs.
 */
class NewTabView : View() {

    override val root = flowpane {
        padding = Insets(5.0)

        mapOf(
            "Web Browser" to { WebBrowserTab() }
        ).forEach { (tabName, tabGenerator) ->
            button(tabName) {
                action {
                    MainWindowView.mainUIEventBus.post(AddTabEvent(tabGenerator()))
                }
            }
        }
    }

    companion object {
        fun create() = NewTabView()
    }
}
