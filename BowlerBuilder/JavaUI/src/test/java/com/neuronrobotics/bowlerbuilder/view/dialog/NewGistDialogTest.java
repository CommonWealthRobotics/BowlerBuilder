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
import java.util.Arrays;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NewGistDialogTest extends AbstractAutoClosingApplicationTest {

  private NewGistDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new NewGistDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  @Disabled
  void basicTest() {
    clickOn("#nameField").write("a.b");
    clickOn("#descField").write("b");

    assertFalse(lookup("OK").query().isDisabled());

    clickOn("OK");

    assertEquals(Arrays.asList("a.b", "b"), dialog.getResult());
    assertEquals("a.b", dialog.getName());
    assertEquals("b", dialog.getDescription());
    assertTrue(dialog.isPublic());
  }

  @Test
  @Disabled
  void onlyNameTest() {
    clickOn("#nameField").write("a");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  @Disabled
  void onlyDescTest() {
    clickOn("#descField").write("b");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  @Disabled
  void invalidNameTest1() {
    clickOn("#nameField").write("a");
    clickOn("#descField").write("b");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  @Disabled
  void invalidNameTest2() {
    clickOn("#nameField").write("a.");
    clickOn("#descField").write("b");

    assertTrue(lookup("OK").query().isDisabled());
  }
}
