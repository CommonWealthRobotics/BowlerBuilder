package com.neuronrobotics.bowlerbuilder.view.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NewCylinderDialog extends Dialog<List<String>> {

  private static final ButtonType ADD = new ButtonType("Add", ButtonBar.ButtonData.APPLY);

  private final TextField nameField;
  private final TextField topRadiusField;
  private final TextField bottomRadiusField;
  private final TextField heightField;
  private final TextField resolutionField;

  public NewCylinderDialog() {
    super();

    nameField = new TextField();
    topRadiusField = new TextField();
    bottomRadiusField = new TextField();
    heightField = new TextField();
    resolutionField = new TextField();

    nameField.setId("nameField");
    topRadiusField.setId("topRadiusField");
    bottomRadiusField.setId("bottomRadiusField");
    heightField.setId("heightField");
    resolutionField.setId("resolutionField");

    setTitle("New Cube");

    GridPane pane = new GridPane();
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(new Label("Width"), 0, 1);
    pane.add(new Label("Length"), 0, 2);
    pane.add(new Label("Height"), 0, 3);
    pane.add(new Label("Resolution"), 0, 4);
    pane.add(nameField, 1, 0);
    pane.add(topRadiusField, 1, 1);
    pane.add(bottomRadiusField, 1, 2);
    pane.add(heightField, 1, 3);
    pane.add(resolutionField, 1, 4);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ADD, ButtonType.CANCEL);

    Platform.runLater(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ADD);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!nameField.getText().isEmpty()
                && !topRadiusField.getText().isEmpty()
                && !bottomRadiusField.getText().isEmpty()
                && !heightField.getText().isEmpty()
                && !resolutionField.getText().isEmpty()),
        nameField.textProperty(),
        topRadiusField.textProperty(),
        bottomRadiusField.textProperty(),
        heightField.textProperty(),
        resolutionField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType == ADD) {
        List<String> data = new ArrayList<>();
        Collections.addAll(data,
            nameField.getText(),
            topRadiusField.getText(),
            bottomRadiusField.getText(),
            heightField.getText(),
            resolutionField.getText());
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
    return "CSG " + nameField.getText() + " = new Cylinder("
        + topRadiusField.getText() + ", "
        + bottomRadiusField.getText() + ", "
        + heightField.getText() + ", "
        + resolutionField.getText() + ").toCSG();";
  }
}
