/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferenceListener;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import java.util.Collections;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class PreferencesDialogTest extends AbstractAutoClosingApplicationTest {

  private Stage stage;

  @Override
  public void start(final Stage stage) {
    this.stage = stage;
  }

  @Test
  void valueChangedTest() {
    final PreferencesService service = new PreferencesService("");
    service.set("a", "foo");
    service.set("b", 99);

    FxHelper.runAndWait(
        () -> {
          final PreferencesDialog dialog =
              new PreferencesDialog(Collections.singletonList(service));
          stage.setScene(dialog.getDialogPane().getScene());
          stage.show();
        });

    doubleClickOn("foo").write("bar");
    doubleClickOn("99").press(KeyCode.DELETE).press(KeyCode.DELETE).write("98");
    clickOn("OK");

    assertEquals("bar", service.get("a", "baz"));
    assertEquals(Integer.valueOf(98), service.get("b", 100));
  }

  @Test
  void listenerTest() {
    final PreferencesService service = new PreferencesService("");
    service.set("a", "foo");
    final BooleanProperty val = new SimpleBooleanProperty(false);
    service.addListener("a", (PreferenceListener<String>) (oldVal, newVal) -> val.setValue(true));

    FxHelper.runAndWait(
        () -> {
          final PreferencesDialog dialog =
              new PreferencesDialog(Collections.singletonList(service));
          stage.setScene(dialog.getDialogPane().getScene());
          stage.show();
        });

    doubleClickOn("foo").write("bar");
    clickOn("OK");

    assertTrue(val.getValue());
  }
}
