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
package com.neuronrobotics.bowlerbuilder.view.main

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.view.main.event.ActionRunningEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.ActionStoppedEvent
import org.greenrobot.eventbus.Subscribe
import tornadofx.*

/**
 * Displays which IDE actions are currently running. Send a [ActionRunningEvent] when an action
 * starts running and send a [ActionStoppedEvent] when an action stops running.
 */
class RunningActionsView : Fragment() {

    private val processView = RunningProcessView<String>("actions") {}

    override val root = processView.root

    init {
        MainWindowController.mainUIEventBus.register(this)
    }

    @Subscribe
    fun onActionRunningEvent(event: ActionRunningEvent) {
        processView.addProcess(event.name, event.name)
    }

    @Subscribe
    fun onActionRunningEvent(event: ActionStoppedEvent) {
        processView.removeProcess(event.name)
    }
}
