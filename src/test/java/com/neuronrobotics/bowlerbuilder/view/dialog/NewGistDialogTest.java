package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import java.util.Arrays;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class NewGistDialogTest extends AutoClosingApplicationTest {

  private NewGistDialog dialog;

  @Override
  public void start(Stage stage) {
    dialog = new NewGistDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void basicTest() {
    clickOn("#nameField").write("a");
    clickOn("#descField").write("b");

    assertFalse(lookup("OK").query().isDisabled());

    clickOn("OK");

    assertEquals(Arrays.asList("a", "b"), dialog.getResult());
    assertEquals("a", dialog.getName());
    assertEquals("b", dialog.getDescription());
    assertTrue(dialog.getIsPublic());
  }

  @Test
  void onlyNameTest() {
    clickOn("#nameField").write("a");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void onlyDescTest() {
    clickOn("#descField").write("b");

    assertTrue(lookup("OK").query().isDisabled());
  }

}
