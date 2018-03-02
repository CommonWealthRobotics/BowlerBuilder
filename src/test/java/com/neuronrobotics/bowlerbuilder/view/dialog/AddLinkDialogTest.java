package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AddLinkDialogTest extends AutoClosingApplicationTest {

  private AddLinkDialog dialog;

  @Override
  public void start(Stage stage) {
    final Set<Integer> set = new HashSet<>();
    set.add(1);
    set.add(2);

    dialog = new AddLinkDialog(set);
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void normalTest() {
    clickOn("#linkNameField").write("name");
    clickOn("#hwIndexField").write("3");

    assertFalse(lookup("OK").query().isDisabled());

    clickOn("OK");
    assertEquals("name", dialog.getResult()[0]);
    assertEquals("3", dialog.getResult()[1]);
  }

  @Test
  void emptyNameTest() {
    doubleClickOn("#linkNameField").type(KeyCode.BACK_SPACE);
    clickOn("#hwIndexField").write("3");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void invalidHWIndicesTest1() {
    clickOn("#linkNameField").write("name");
    clickOn("#hwIndexField").write("1");

    assertTrue(lookup("OK").query().isDisabled());
  }

  @Test
  void invalidHWIndicesTest2() {
    doubleClickOn("#linkNameField").type(KeyCode.BACK_SPACE);
    clickOn("#hwIndexField").write("1");

    assertTrue(lookup("OK").query().isDisabled());
  }

}
