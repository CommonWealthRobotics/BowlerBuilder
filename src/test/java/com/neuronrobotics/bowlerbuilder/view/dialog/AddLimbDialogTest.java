/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AddLimbDialogTest extends AbstractAutoClosingApplicationTest {

  private AddLimbDialog dialog;

  @Override
  public void start(final Stage stage) {
    final Set<Integer> set = new HashSet<>();
    set.add(1);
    set.add(2);

    dialog = new AddLimbDialog("name", 2, set);
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void emptyNameTest() {
    doubleClickOn("#linkNameField").type(KeyCode.BACK_SPACE);
    clickOn("#hwIndexField1").write("3");
    clickOn("#hwIndexField2").write("4");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void validHWIndicesTest1() {
    clickOn("#hwIndexField1").write("3");
    clickOn("#hwIndexField2").write("4");

    assertFalse(lookup("OK").query().isDisabled());
  }

  @Test
  void validHWIndicesTest2() {
    clickOn("#hwIndexField1").write("1");
    clickOn("#hwIndexField2").write("4");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void validHWIndicesTest3() {
    clickOn("#hwIndexField1").write("1");
    clickOn("#hwIndexField2").write("2");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void validHWIndicesTest4() {
    clickOn("#hwIndexField1").write("3");
    clickOn("#hwIndexField2").write("3");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void cancelTest() {
    doubleClickOn("#linkNameField").write("a");
    clickOn("#hwIndexField1").write("3");
    clickOn("#hwIndexField2").write("4");
    clickOn("Cancel");

    assertNull(dialog.getResult());
  }
}
