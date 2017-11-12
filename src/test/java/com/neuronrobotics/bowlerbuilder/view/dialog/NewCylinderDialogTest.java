package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class NewCylinderDialogTest extends CADAcceleratorDialogTest<NewCylinderDialog> {

  NewCylinderDialogTest() {
    super(NewCylinderDialog::new);
  }

  @Test
  void testCodeGen() {
    clickOn("#nameField").write("foo");
    clickOn("#topRadiusField").write("1");
    clickOn("#bottomRadiusField").write("2");
    clickOn("#heightField").write("3");
    clickOn("#resolutionField").write("4");
    assertEquals("CSG foo = new Cylinder(1, 2, 3, 4).toCSG();",
        dialog.getResultAsScript());
  }

}
