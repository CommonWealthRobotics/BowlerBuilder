package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

class NewCubeDialogTest extends CADAcceleratorDialogTest<NewCubeDialog> {

  private static final ButtonType ADD = new ButtonType("Add", ButtonBar.ButtonData.APPLY);

  NewCubeDialogTest() {
    super(NewCubeDialog::new);
  }

  @Test
  void codeGenTest() {
    ((TextField)lookup("#nameField").query()).setText("foo");
    ((TextField)lookup("#widthField").query()).setText("1");
    ((TextField)lookup("#lengthField").query()).setText("2");
    ((TextField)lookup("#heightField").query()).setText("3");
    assertEquals("CSG foo = new Cube(1, 2, 3).toCSG();", dialog.getResultAsScript());
  }

  @Test
  void resultTest() {
    ((TextField)lookup("#nameField").query()).setText("foo");
    ((TextField)lookup("#widthField").query()).setText("1");
    ((TextField)lookup("#lengthField").query()).setText("2");
    ((TextField)lookup("#heightField").query()).setText("3");

    List<String> result = dialog.getResultConverter().call(ButtonType.OK);

    assertTrue("foo".equals(result.get(0)));
    assertTrue("1".equals(result.get(1)));
    assertTrue("2".equals(result.get(2)));
    assertTrue("3".equals(result.get(3)));
  }

}
