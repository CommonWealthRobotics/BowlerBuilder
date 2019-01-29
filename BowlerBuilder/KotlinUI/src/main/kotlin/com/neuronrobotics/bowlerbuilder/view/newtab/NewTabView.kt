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
package com.neuronrobotics.bowlerbuilder.view.newtab

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
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
                    MainWindowController.mainUIEventBus.post(
                        AddTabEvent(tabGenerator())
                    )
                }
            }
        }
    }

    companion object {
        fun create() = NewTabView()
    }
}
