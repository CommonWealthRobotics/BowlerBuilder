package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.testfx.util.WaitForAsyncUtils;

public class AddFileToGistDialogTest extends AutoClosingApplicationTest {

  private static TestReporter REPORTER;
  private AddFileToGistDialog dialog;

  @BeforeAll
  static void injectReporter(final TestReporter reporter) {
    REPORTER = reporter;
  }

  @Override
  public void start(Stage stage) {
    REPORTER.publishEntry("Method call", "Start being called!");
    dialog = new AddFileToGistDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  /*@Test
  void validFileNameTest() {
    clickOn("#nameField").write("a");

    assertTrue(dialog.isInvalidName());
    assertTrue(lookup("OK").query().isDisabled());
  }*/

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
