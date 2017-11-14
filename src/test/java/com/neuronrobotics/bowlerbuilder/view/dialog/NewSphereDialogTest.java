package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.FxHelper;
import java.util.List;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

class NewSphereDialogTest extends CADAcceleratorDialogTest<NewSphereDialog> {

  NewSphereDialogTest() {
    super(NewSphereDialog::new);
  }

  private void fillValues() {
    FxHelper.runAndWait(() -> {
      ((TextField) lookup("#nameField").query()).setText("foo");
      ((TextField) lookup("#radiusField").query()).setText("1");
    });
  }

  @Test
  void codeGenTest() {
    fillValues();
    assertEquals("CSG foo = new Sphere(1).toCSG();", dialog.getResultAsScript());
  }

  @Test
  void resultTest() {
    fillValues();
    List<String> result = dialog.getResultConverter().call(ButtonType.OK);

    assertTrue("foo".equals(result.get(0)));
    assertTrue("1".equals(result.get(1)));
  }

}
