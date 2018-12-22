/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LoginDialogTest extends AbstractAutoClosingApplicationTest {

  private LoginDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new LoginDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  @Disabled
  void basicTest() {
    clickOn("#loginNameField").write("a");
    clickOn("#loginPassField").write("b");
    clickOn("OK");

    assertTrue(dialog.getResult());
    assertEquals("a", dialog.getName());
    assertEquals("b", dialog.getPassword());
  }

  @Test
  @Disabled
  void cancelTest() {
    clickOn("#loginNameField").write("a");
    clickOn("#loginPassField").write("b");
    clickOn("Cancel");

    assertFalse(dialog.getResult());
  }

  @Test
  @Disabled
  void nameOnlyTest() {
    clickOn("#loginNameField").write("a");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  @Disabled
  void passOnlyTest() {
    clickOn("#loginPassField").write("b");

    assertTrue(lookup("OK").query().isDisabled());
  }
}
