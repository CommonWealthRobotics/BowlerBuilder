/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.model.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class PreferencesServiceTest {

  @Test
  void get() {
    PreferencesService preferencesService = new PreferencesService("");
    assertEquals("a", preferencesService.get("foo", "a"));
  }

  @Test
  void prefixListenerTest() {
    PreferencesService preferencesService = new PreferencesService("");
    BooleanProperty test = new SimpleBooleanProperty(false);
    preferencesService.set("foo", "a");
    preferencesService.addListener("foo", (String oldVal, String newVal) ->
        test.setValue(true));
    assertFalse(test.getValue());
  }

  @Test
  void postfixListenerTest() {
    PreferencesService preferencesService = new PreferencesService("");
    BooleanProperty test = new SimpleBooleanProperty(false);
    preferencesService.addListener("foo", (oldVal, newVal) -> test.setValue(true));
    preferencesService.set("foo", "a");
    assertTrue(test.getValue());
  }

  @Test
  void set() {
    PreferencesService preferencesService = new PreferencesService("");
    preferencesService.set("foo", "b");
    assertEquals("b", preferencesService.get("foo", "a"));
  }

  @Test
  void getAllValues() {
    PreferencesService preferencesService = new PreferencesService("");
    preferencesService.set("foo", "a");
    preferencesService.set("bar", "b");
    assertTrue(CollectionUtils.isEqualCollection(
        Arrays.asList("a", "b"),
        preferencesService.getAllValues()));
  }

  @Test
  void getAll() {
    PreferencesService preferencesService = new PreferencesService("");
    preferencesService.set("foo", "a");
    preferencesService.set("bar", "b");
    Map<String, String> test = new HashMap<>();
    test.put("foo", "a");
    test.put("bar", "b");
    assertEquals(test, preferencesService.getAll());
  }

}
