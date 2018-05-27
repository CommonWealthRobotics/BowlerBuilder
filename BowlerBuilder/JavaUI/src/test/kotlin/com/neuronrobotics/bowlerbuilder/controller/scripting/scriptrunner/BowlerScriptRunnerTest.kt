/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isA
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerGroovy
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerScriptRunner
import eu.mihosoft.vrl.v3d.CSG
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class BowlerScriptRunnerTest {

    private val language = BowlerGroovy()
    private val languageName = language.shellType
    private val runner = BowlerScriptRunner(language)

    @Test
    fun `no code returns null`() {
        val result = runBasicScript("")

        assertAll(
                { assertTrue(result.isSuccess()) },
                { assertNull(result.getSuccess()) }
        )
    }

    @Test
    fun `simple cube returns a CSG`() {
        val result = runBasicScript("CSG foo=new Cube(10,10,10).toCSG()")

        assertAll(
                { assertTrue(result.isSuccess()) },
                { assertNotNull(result.getSuccess()) },
                { assertThat(result.getSuccess()!!, isA<CSG>()) }
        )
    }

    @Test
    fun `exception in script returns a failure`() {
        val result = runBasicScript("throw new RuntimeException(\"Hello, World\"!")

        assertAll(
                { assertTrue(result.isError()) },
                { assertNotNull(result.getError()) },
                { assertThat(result.getError()!!, isA<RuntimeException>()) }
        )
    }

    private fun runBasicScript(text: String) =
            runner.runScript(script = text, arguments = null, languageName = languageName)
}
