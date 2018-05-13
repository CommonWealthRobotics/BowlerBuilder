/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.model.preferences

import javafx.beans.property.SimpleBooleanProperty
import org.apache.commons.collections.CollectionUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.Serializable

class PreferencesServiceTest {

    @Test
    fun get() {
        val preferencesService = PreferencesService("")
        assertEquals("a", preferencesService.get("foo", "a"))
    }

    @Test
    fun `attaching a listener after setting the preference does not fire the listener`() {
        val preferencesService = PreferencesService("")
        preferencesService.set("foo", "a")

        val test = SimpleBooleanProperty(false)
        preferencesService.addListener<String>("foo") { _, _ -> test.value = true }

        assertFalse(test.value)
    }

    @Test
    fun `attaching a listener before setting the preference fires the listener`() {
        val preferencesService = PreferencesService("")

        val test = SimpleBooleanProperty(false)
        preferencesService.addListener<Serializable>("foo") { _, _ -> test.value = true }
        preferencesService.set("foo", "a")

        assertTrue(test.value)
    }

    @Test
    fun `simple set test`() {
        val preferencesService = PreferencesService("")
        preferencesService.set("foo", "b")

        assertEquals("b", preferencesService.get("foo", "a"))
    }

    @Test
    fun `simple set multiple test`() {
        val preferencesService = PreferencesService("")
        preferencesService.set("foo", "a")
        preferencesService.set("bar", "b")

        assertTrue(CollectionUtils.isEqualCollection(
                listOf("a", "b"),
                preferencesService.allValues))
    }

    @Test
    fun getAll() {
        val preferencesService = PreferencesService("")
        preferencesService.set("foo", "a")
        preferencesService.set("bar", "b")

        val test = HashMap<String, String>()
        test["foo"] = "a"
        test["bar"] = "b"

        assertEquals(test, preferencesService.all)
    }
}
