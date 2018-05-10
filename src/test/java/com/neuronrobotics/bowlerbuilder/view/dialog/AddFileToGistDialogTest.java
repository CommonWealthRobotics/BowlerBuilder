/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

public class AddFileToGistDialogTest extends AbstractAutoClosingApplicationTest {

  private AddFileToGistDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new AddFileToGistDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void validFileNameTest() {
    clickOn("#nameField").write("a");

    assertTrue(dialog.isInvalidName());
    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  @Tag("RunMe")
  void validFileNameTest2() {
    ((TextField) lookup("#nameField").query()).setText("a.");
    WaitForAsyncUtils.waitForFxEvents();

    assertTrue(dialog.isInvalidName());
    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void validFileNameTest3() {
    clickOn("#nameField").write("a.b");

    assertFalse(dialog.isInvalidName());
    assertFalse(lookup("OK").query().isDisabled());

    clickOn("OK");

    assertEquals("a.b", dialog.getResult());
    assertEquals("a.b", dialog.getName());
  }

  @Test
  void cancelTest() {
    clickOn("#nameField").write("a.b").clickOn("Cancel");

    assertNull(dialog.getResult());
  }
}
