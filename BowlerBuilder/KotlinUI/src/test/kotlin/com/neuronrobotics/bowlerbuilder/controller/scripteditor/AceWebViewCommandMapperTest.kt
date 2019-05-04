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
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
