package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class NewCubeDialogTest extends CADAcceleratorDialogTest<NewCubeDialog> {

  NewCubeDialogTest() {
    super(NewCubeDialog::new);
  }

  @Test
  void testCodeGen() {
    clickOn("#nameField").write("foo");
    clickOn("#widthField").write("1");
    clickOn("#lengthField").write("2");
    clickOn("#heightField").write("3");
    assertEquals("CSG foo = new Cube(1, 2, 3).toCSG();", dialog.getResultAsScript());
  }

}
