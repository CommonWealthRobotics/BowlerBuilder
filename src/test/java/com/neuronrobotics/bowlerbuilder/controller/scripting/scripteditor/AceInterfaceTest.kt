/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditor
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test

class AceInterfaceTest {

    private val mockAdapter = MockAdapter()
    private val aceEditor = AceEditor(mockAdapter)

    @Test
    internal fun insertTextTest() {
        aceEditor.insertAtCursor("test")
        assertEquals("editor.insert(\"test\");", mockAdapter.lastExecutedScript)
    }

    @Test
    internal fun insertTextTest2() {
        aceEditor.insertAtCursor("CSG foo = new Cube(1,1,1).toCSG();")
        assertEquals("editor.insert(\"CSG foo = new Cube(1,1,1).toCSG();\");",
                mockAdapter.lastExecutedScript)
    }

    @Test
    internal fun insertTextTest3() {
        aceEditor.insertAtCursor("\n\t")
        assertEquals("editor.insert(\"\\n\t\");",
                mockAdapter.lastExecutedScript)
    }

    @Test
    internal fun insertTextTest4() {
        aceEditor.insertAtCursor("\\'\"")
        assertEquals("editor.insert(\"\\\\'\\\"\");",
                mockAdapter.lastExecutedScript)
    }

    @Test
    internal fun fontSizeTest() {
        aceEditor.setFontSize(1)
        assertEquals("document.getElementById('editor').style.fontSize='1px';",
                mockAdapter.lastExecutedScript)
    }

    @Test
    internal fun gotoLineTest() {
        aceEditor.gotoLine(1)
        assertEquals("editor.gotoLine(1);",
                mockAdapter.lastExecutedScript)
    }
}
