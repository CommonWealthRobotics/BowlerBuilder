package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AddFileToGistDialogTest extends AutoClosingApplicationTest {

  private AddFileToGistDialog dialog;

  @Override
  public void start(Stage stage) {
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
  void validFileNameTest2() {
    clickOn("#nameField").write("a.");

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
