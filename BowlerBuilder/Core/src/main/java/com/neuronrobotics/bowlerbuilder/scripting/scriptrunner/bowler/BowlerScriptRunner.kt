/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler

import com.google.inject.Inject
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner
import com.neuronrobotics.bowlerbuilder.util.Verified
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty

class BowlerScriptRunner @Inject constructor(language: BowlerGroovy) : ScriptRunner {

    private var result: Verified<Exception, Any?> = Verified.success(null)
    private val resultProperty = SimpleObjectProperty<Verified<Exception, Any?>>()
    private val running = SimpleBooleanProperty(false)

    init {
        ScriptingEngine.addScriptingLanguage(language)
    }

    override fun runScript(
        scriptGitUrl: String,
        scriptFilename: String
    ): Verified<Exception, Any?> {
        val file = ScriptingEngine.fileFromGit(scriptGitUrl, scriptFilename)
        return runScript(file.readText(), null, ScriptingEngine.getShellType(scriptFilename))
    }

    override fun runScript(script: String, arguments: ArrayList<Any>?, languageName: String):
        Verified<Exception, Any?> {
        return try {
            running.value = true
            val output = ScriptingEngine.inlineScriptStringRun(script, arguments, languageName)
            result = Verified.success(output)
            result
        } catch (e: Exception) {
            result = Verified.error(e)
            result
        } finally {
            running.value = false
            resultProperty.value = result
        }
    }

    override fun getResult(): Verified<Exception, Any?> = result

    override fun resultProperty(): ReadOnlyObjectProperty<Verified<Exception, Any?>> =
        resultProperty

    override fun isScriptRunning(): Boolean = running.value

    override fun scriptRunningProperty(): ReadOnlyBooleanProperty = running
}
