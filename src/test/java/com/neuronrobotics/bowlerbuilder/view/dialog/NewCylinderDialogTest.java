package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.FxHelper;
import java.util.List;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

class NewCylinderDialogTest extends CADAcceleratorDialogTest<NewCylinderDialog> {

  NewCylinderDialogTest() {
    super(NewCylinderDialog::new);
  }

  private void fillValues() {
    FxHelper.runAndWait(() -> {
      ((TextField)lookup("#nameField").query()).setText("foo");
      ((TextField)lookup("#topRadiusField").query()).setText("1");
      ((TextField)lookup("#bottomRadiusField").query()).setText("2");
      ((TextField)lookup("#heightField").query()).setText("3");
      ((TextField)lookup("#resolutionField").query()).setText("5");
    });
  }

  @Test
  void codeGenTest() {
    fillValues();
    assertEquals("CSG foo = new Cylinder(1, 2, 3, 5).toCSG();",
        dialog.getResultAsScript());
  }

  @Test
  void resultTest() {
    fillValues();
    List<String> result = dialog.getResultConverter().call(ButtonType.OK);

    assertTrue("foo".equals(result.get(0)));
    assertTrue("1".equals(result.get(1)));
    assertTrue("2".equals(result.get(2)));
    assertTrue("3".equals(result.get(3)));
    assertTrue("5".equals(result.get(4)));
  }

}
