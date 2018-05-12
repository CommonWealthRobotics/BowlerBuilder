/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.util

import org.junit.Assert.assertEquals

import org.junit.jupiter.api.Test

class StringClipperTest {

    private val stringClipper = StringClipper()

    @Test
    internal fun emptyInputTest() {
        assertEquals("", stringClipper.clipStringToLines("", 1))
    }

    @Test
    internal fun emptyInput0Test() {
        assertEquals("", stringClipper.clipStringToLines("", 0))
    }

    @Test
    internal fun simpleInputTest() {
        assertEquals("test", stringClipper.clipStringToLines("test", 1))
    }

    @Test
    internal fun simpleInput0Test() {
        assertEquals("", stringClipper.clipStringToLines("test", 0))
    }

    @Test
    internal fun negativeLinesTest() {
        assertEquals("", stringClipper.clipStringToLines("test", -1))
    }

    @Test
    internal fun oneLineTest() {
        assertEquals("a", stringClipper.clipStringToLines("a\nb", 1))
    }

    @Test
    internal fun twoLineTest() {
        assertEquals("a\nb", stringClipper.clipStringToLines("a\nb", 2))
    }

    @Test
    internal fun lineOOBTest() {
        assertEquals("a", stringClipper.clipStringToLines("a", 2))
    }
}
