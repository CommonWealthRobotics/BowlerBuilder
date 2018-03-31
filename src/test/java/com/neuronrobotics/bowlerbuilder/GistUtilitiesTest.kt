package com.neuronrobotics.bowlerbuilder

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

internal class GistUtilitiesTest {

    @Test
    fun testIsValidCodeFileName1() {
        assertTrue(GistUtilities.isValidCodeFileName("a.b").isPresent)
    }

    @Test
    fun testIsValidCodeFileName2() {
        assertFalse(GistUtilities.isValidCodeFileName("a").isPresent)
    }

    @Test
    fun testIsValidCodeFileName3() {
        assertFalse(GistUtilities.isValidCodeFileName("a.").isPresent)
    }

    @Test
    fun testIsValidCodeFileName4() {
        assertFalse(GistUtilities.isValidCodeFileName("").isPresent)
    }

}
