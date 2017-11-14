package com.neuronrobotics.bowlerbuilder.view.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NewCubeDialog extends Dialog<List<String>> {

  private final TextField nameField;
  private final TextField widthField;
  private final TextField lengthField;
  private final TextField heightField;

  public NewCubeDialog() {
    super();

    nameField = new TextField();
    widthField = new TextField();
    lengthField = new TextField();
    heightField = new TextField();

    nameField.setId("nameField");
    widthField.setId("widthField");
    lengthField.setId("lengthField");
    heightField.setId("heightField");

    setTitle("New Cube");

    GridPane pane = new GridPane();
    pane.setId("newCubeRoot");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(new Label("Width"), 0, 1);
    pane.add(new Label("Length"), 0, 2);
    pane.add(new Label("Height"), 0, 3);
    pane.add(nameField, 1, 0);
    pane.add(widthField, 1, 1);
    pane.add(lengthField, 1, 2);
    pane.add(heightField, 1, 3);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Platform.runLater(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!nameField.getText().isEmpty()
                && !widthField.getText().isEmpty()
                && !lengthField.getText().isEmpty()
                && !heightField.getText().isEmpty()),
        nameField.textProperty(),
        widthField.textProperty(),
        lengthField.textProperty(),
        heightField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        List<String> data = new ArrayList<>();
        Collections.addAll(data,
            nameField.getText(),
            widthField.getText(),
            lengthField.getText(),
            heightField.getText());
        return data;
      }
      return null;
    });
  }

  /**
   * Return the inputs to this dialog as a line of code. Validation is not performed.
   *
   * @return The code form of the dialog's inputs
   */
  public String getResultAsScript() {
    return "CSG " + nameField.getText() + " = new Cube("
        + widthField.getText() + ", "
        + lengthField.getText() + ", "
        + heightField.getText() + ").toCSG();";
  }
}
