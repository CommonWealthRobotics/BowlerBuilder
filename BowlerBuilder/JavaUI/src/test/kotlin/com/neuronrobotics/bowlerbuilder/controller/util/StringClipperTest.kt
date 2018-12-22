/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.util

import com.neuronrobotics.bowlerbuilder.util.StringClipper
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Disabled

import org.junit.jupiter.api.Test

class StringClipperTest {

    private val stringClipper = StringClipper()

    @Test
    @Disabled
    fun emptyInputTest() {
        assertEquals("", stringClipper.clipStringToLines("", 1))
    }

    @Test
    @Disabled
    fun emptyInput0Test() {
        assertEquals("", stringClipper.clipStringToLines("", 0))
    }

    @Test
    @Disabled
    fun simpleInputTest() {
        assertEquals("test", stringClipper.clipStringToLines("test", 1))
    }

    @Test
    @Disabled
    fun simpleInput0Test() {
        assertEquals("", stringClipper.clipStringToLines("test", 0))
    }

    @Test
    @Disabled
    fun negativeLinesTest() {
        assertEquals("", stringClipper.clipStringToLines("test", -1))
    }

    @Test
    @Disabled
    fun oneLineTest() {
        assertEquals("a", stringClipper.clipStringToLines("a\nb", 1))
    }

    @Test
    @Disabled
    fun twoLineTest() {
        assertEquals("a\nb", stringClipper.clipStringToLines("a\nb", 2))
    }

    @Test
    @Disabled
    fun lineOOBTest() {
        assertEquals("a", stringClipper.clipStringToLines("a", 2))
    }
}
