/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.util

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import eu.mihosoft.vrl.v3d.CSG
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test

class CsgParserTest {

    @Test
    fun parseCsgTest() {
        val script = "CSG foo = new Cube(1,1,1).toCSG();"
        val result = ScriptingEngine.inlineScriptStringRun(script, null, "Groovy") as CSG

        val csgMap = mapOf(result to result.mesh)
        val parser = CsgParser()
        val test = parser.parseCsgFromSource("Script1", 1, csgMap)

        assertEquals(result, test.iterator().next())
    }
}
