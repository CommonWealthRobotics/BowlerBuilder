/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace.WebEngineAdapter
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Worker

class MockAdapter : WebEngineAdapter {

    var lastExecutedScript = ""

    override fun executeScript(script: String): Any? {
        lastExecutedScript = script
        return null
    }

    @SuppressWarnings("ComplexMethod", "ForbiddenVoid")
    override fun getLoadWorker(): Worker<Void> {
        return object : Worker<Void> {
            override fun getState(): Worker.State {
                return Worker.State.SUCCEEDED
            }

            override fun stateProperty(): ReadOnlyObjectProperty<Worker.State> {
                return SimpleObjectProperty(Worker.State.SUCCEEDED)
            }

            override fun getValue(): Void? {
                return null
            }

            override fun valueProperty(): ReadOnlyObjectProperty<Void>? {
                return null
            }

            override fun getException(): Throwable? {
                return null
            }

            override fun exceptionProperty(): ReadOnlyObjectProperty<Throwable>? {
                return null
            }

            override fun getWorkDone(): Double {
                return 0.0
            }

            override fun workDoneProperty(): ReadOnlyDoubleProperty? {
                return null
            }

            override fun getTotalWork(): Double {
                return 0.0
            }

            override fun totalWorkProperty(): ReadOnlyDoubleProperty? {
                return null
            }

            override fun getProgress(): Double {
                return 0.0
            }

            override fun progressProperty(): ReadOnlyDoubleProperty? {
                return null
            }

            override fun isRunning(): Boolean {
                return false
            }

            override fun runningProperty(): ReadOnlyBooleanProperty? {
                return null
            }

            override fun getMessage(): String? {
                return null
            }

            override fun messageProperty(): ReadOnlyStringProperty? {
                return null
            }

            override fun getTitle(): String? {
                return null
            }

            override fun titleProperty(): ReadOnlyStringProperty? {
                return null
            }

            override fun cancel(): Boolean {
                return false
            }
        }
    }
}
