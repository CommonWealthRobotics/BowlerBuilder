/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.model.preferences

import javafx.beans.property.SimpleBooleanProperty
import org.apache.commons.collections.CollectionUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.util.*

internal class PreferencesServiceTest {

    @Test
    fun get() {
        val preferencesService = PreferencesService("")
        assertEquals("a", preferencesService.get("foo", "a"))
    }

    @Test
    fun prefixListenerTest() {
        val preferencesService = PreferencesService("")
        val test = SimpleBooleanProperty(false)
        preferencesService.set("foo", "a")
        preferencesService.addListener("foo") { _: String, _: String -> test.value = true }
        assertFalse(test.value)
    }

    @Test
    fun postfixListenerTest() {
        val preferencesService = PreferencesService("")
        val test = SimpleBooleanProperty(false)
        preferencesService.addListener<Serializable>("foo") { _, _ -> test.value = true }
        preferencesService.set("foo", "a")
        assertTrue(test.value)
    }

    @Test
    fun set() {
        val preferencesService = PreferencesService("")
        preferencesService.set("foo", "b")
        assertEquals("b", preferencesService.get("foo", "a"))
    }

    @Test
    fun getAllValues() {
        val preferencesService = PreferencesService("")
        preferencesService.set("foo", "a")
        preferencesService.set("bar", "b")
        assertTrue(CollectionUtils.isEqualCollection(
                Arrays.asList("a", "b"),
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
