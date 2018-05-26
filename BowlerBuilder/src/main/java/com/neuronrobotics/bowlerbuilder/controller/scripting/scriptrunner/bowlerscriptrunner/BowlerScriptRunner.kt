/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.bowlerscriptrunner

import com.google.inject.Inject
import com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.ScriptRunner
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import fj.data.Validation
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty

class BowlerScriptRunner @Inject constructor(private val language: BowlerGroovy) : ScriptRunner {
    private val result: ObjectProperty<Validation<Exception, Any?>>

    init {
        result = SimpleObjectProperty()
        ScriptingEngine.addScriptingLanguage(language)
    }

    override fun runScript(script: String, arguments: ArrayList<Any>?, languageName: String):
            Validation<Exception, Any?> {
        return try {
            val output = ScriptingEngine.inlineScriptStringRun(script, arguments, languageName)
            result.value = Validation.success(output)
            result.value
        } catch (e: Exception) {
            result.value = Validation.fail(e)
            result.value
        }
    }

    override fun isScriptRunning(): Boolean = language.runningProperty().value

    override fun scriptRunningProperty(): ReadOnlyBooleanProperty = language.runningProperty()

    override fun resultProperty(): ReadOnlyObjectProperty<Validation<Exception, Any?>> = result
}
