/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class AceWebViewCommandMapperTest {

    private val mapper = AceWebViewCommandMapper()

    @Test
    fun insertTextTest() {
        assertEquals("editor.insert(\"test\");", mapper.insertAtCursor("test"))
    }

    @Test
    fun insertTextTest2() {
        assertEquals(
            "editor.insert(\"CSG foo = new Cube(1,1,1).toCSG();\");",
            mapper.insertAtCursor("CSG foo = new Cube(1,1,1).toCSG();")
        )
    }

    @Test
    fun insertTextTest3() {
        assertEquals(
            "editor.insert(\"\\n\t\");",
            mapper.insertAtCursor("\n\t")
        )
    }

    @Test
    fun insertTextTest4() {
        assertEquals(
            "editor.insert(\"\\\\'\\\"\");",
            mapper.insertAtCursor("\\'\"")
        )
    }

    @Test
    fun setTextTest1() {
        assertEquals(
            "editor.setValue(\"test\");",
            mapper.setText("test")
        )
    }

    @Test
    fun setTextTest2() {
        assertEquals(
            "editor.setValue(\"\\\\'\\\"\");",
            mapper.setText("\\'\"")
        )
    }

    @Test
    fun gotoLineTest() {
        assertEquals(
            "editor.gotoLine(1);",
            mapper.gotoLine(1)
        )
    }

    @Test
    fun getFullTextTest() {
        assertEquals(
            "editor.getValue();",
            mapper.getFullText()
        )
    }

    @Test
    fun getSelectedTextTest() {
        assertEquals(
            "editor.session.getTextRange(editor.getSelectionRange());",
            mapper.getSelectedText()
        )
    }

    @Test
    fun getCursorPositionTest() {
        assertEquals(
            "editor.session.doc.positionToIndex(editor.selection.getCursor());",
            mapper.getCursorPosition()
        )
    }
}
