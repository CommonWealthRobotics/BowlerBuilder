package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class NewRoundedCubeDialogTest extends CADAcceleratorDialogTest<NewRoundedCubeDialog> {

  NewRoundedCubeDialogTest() {
    super(NewRoundedCubeDialog::new);
  }

  @Test
  void testCodeGen() {
    clickOn("#nameField").write("foo");
    clickOn("#widthField").write("1");
    clickOn("#lengthField").write("2");
    clickOn("#heightField").write("3");
    clickOn("#cornerRadiusField").write("4");
    assertEquals("CSG foo = new RoundedCube(1, 2, 3).cornerRadius(4).toCSG();",
        dialog.getResultAsScript());
  }

}
