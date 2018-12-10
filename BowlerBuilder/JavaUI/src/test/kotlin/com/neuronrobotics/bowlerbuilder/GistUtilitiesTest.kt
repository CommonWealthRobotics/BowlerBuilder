/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

class GistUtilitiesTest {

    @Test
    fun `valid filename`() {
        assertTrue(GistUtilities.isValidCodeFileName("a.b").isPresent)
    }

    @Test
    fun `filename can contain underscores`() {
        assertTrue(GistUtilities.isValidCodeFileName("a_c.b").isPresent)
    }

    @Test
    fun `filename can contain hyphens`() {
        assertTrue(GistUtilities.isValidCodeFileName("a-c.b").isPresent)
    }

    @Test
    fun `filename needs an extension`() {
        assertFalse(GistUtilities.isValidCodeFileName("a").isPresent)
    }

    @Test
    fun `filename cannot have an empty extension`() {
        assertFalse(GistUtilities.isValidCodeFileName("a.").isPresent)
    }

    @Test
    fun `filename cannot be empty`() {
        assertFalse(GistUtilities.isValidCodeFileName("").isPresent)
    }

    @Test
    fun `filename cannot contain spaces`() {
        assertFalse(GistUtilities.isValidCodeFileName("a b.x").isPresent)
    }
}
