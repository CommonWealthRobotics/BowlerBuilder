package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class NewSphereDialogTest extends CADAcceleratorDialogTest<NewSphereDialog> {

  NewSphereDialogTest() {
    super(NewSphereDialog::new);
  }

  @Test
  void testCodeGen() {
    clickOn("#nameField").write("foo");
    clickOn("#radiusField").write("1");
    assertEquals("CSG foo = new Sphere(1).toCSG();", dialog.getResultAsScript());
  }

}
