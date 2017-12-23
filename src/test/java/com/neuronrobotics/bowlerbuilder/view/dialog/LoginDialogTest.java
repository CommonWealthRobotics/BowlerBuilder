package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class LoginDialogTest extends AutoClosingApplicationTest {

  private LoginDialog dialog;

  @Override
  public void start(Stage stage) {
    dialog = new LoginDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void basicTest() {
    clickOn("#loginNameField").write("a");
    clickOn("#loginPassField").write("b");
    clickOn("OK");

    assertTrue(dialog.getResult());
    assertEquals("a", dialog.getName());
    assertEquals("b", dialog.getPassword());
  }

  @Test
  void cancelTest() {
    clickOn("#loginNameField").write("a");
    clickOn("#loginPassField").write("b");
    clickOn("Cancel");

    assertFalse(dialog.getResult());
  }

  @Test
  void nameOnlyTest() {
    clickOn("#loginNameField").write("a");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void passOnlyTest() {
    clickOn("#loginPassField").write("b");

    assertTrue(lookup("OK").query().isDisabled());
  }

}
