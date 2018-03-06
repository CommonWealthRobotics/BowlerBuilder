/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.dialog.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AddPluginDialogTest extends AbstractAutoClosingApplicationTest {

  private AddPluginDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new AddPluginDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void basicEntryTest() {
    clickOn("#sourceField").write("url");
    clickOn("#displayNameField").write("name");

    assertFalse(lookup("OK").query().isDisabled());

    clickOn("OK");

    assertTrue(dialog.getResult());
    assertEquals("url", dialog.getSource());
    assertEquals("name", dialog.getDisplayName());
  }

  @Test
  void disabledTest1() {
    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void disabledTest2() {
    clickOn("#sourceField").write("url");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void disabledTest3() {
    clickOn("#displayNameField").write("name");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void cancelTest() {
    clickOn("#sourceField").write("url");
    clickOn("#displayNameField").write("name");
    clickOn("Cancel");

    assertFalse(dialog.getResult());
  }

}
