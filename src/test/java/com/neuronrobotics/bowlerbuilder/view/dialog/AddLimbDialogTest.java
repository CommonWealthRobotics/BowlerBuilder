package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AddLimbDialogTest extends AutoClosingApplicationTest {

  private AddLimbDialog dialog;

  @Override
  public void start(Stage stage) {
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