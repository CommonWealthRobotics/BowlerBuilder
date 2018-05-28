/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.util

import com.google.common.collect.ImmutableList
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerbuilder.cad.CsgParser
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Sphere
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test

class CsgParserTest {

    @Test
    fun `simple one line parse should return the csg`() {
        val script = "CSG foo = new Cube(1,1,1).toCSG();"
        val result = ScriptingEngine.inlineScriptStringRun(script, null, "Groovy") as CSG

        val parser = CsgParser()
        val test = parser.parseCsgFromSource("Script1", 1, ImmutableList.of(result))

        assertEquals(result, test.iterator().next())
    }

    @Test
    fun `multiple csg parse should return the one csg`() {
        val script = """
            CSG foo = new Cube(1,1,1).toCSG();
            CSG bar = new Sphere(1).toCSG();
            return [foo, bar]
        """.trimIndent()

        @Suppress("UNCHECKED_CAST") // We know the type from the script
        val result = ScriptingEngine.inlineScriptStringRun(script, null, "Groovy") as List<CSG>

        val parser = CsgParser()
        val test = parser.parseCsgFromSource("Script1", 1, ImmutableList.copyOf(result))

        assertThat(test, hasSize(equalTo(1)))
        assertEquals(result[0], test[0])
    }

    @Test
    fun `empty script with no input csgs should return nothing`() {
        val script = ""
        ScriptingEngine.inlineScriptStringRun(script, null, "Groovy")

        val parser = CsgParser()
        val test = parser.parseCsgFromSource("Script1", 1, ImmutableList.of())

        assertThat(test, hasSize(equalTo(0)))
    }

    @Test
    fun `empty script with some input csgs should return nothing`() {
        val script = ""
        @Suppress("UNUSED_VARIABLE")
        val result: Any? = ScriptingEngine.inlineScriptStringRun(script, null, "Groovy")

        val parser = CsgParser()
        val test = parser.parseCsgFromSource("Script1", 1, ImmutableList.of(Sphere(1.0).toCSG()))

        assertThat(test, hasSize(equalTo(0)))
    }
}
