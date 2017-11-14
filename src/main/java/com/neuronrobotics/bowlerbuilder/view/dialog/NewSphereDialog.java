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

public class NewSphereDialog extends Dialog<List<String>> {

  private final TextField nameField;
  private final TextField radiusField;

  public NewSphereDialog() {
    super();

    nameField = new TextField();
    radiusField = new TextField();

    nameField.setId("nameField");
    radiusField.setId("radiusField");

    setTitle("New Sphere");

    GridPane pane = new GridPane();
    pane.setId("newSphereRoot");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(new Label("Radius"), 0, 1);
    pane.add(nameField, 1, 0);
    pane.add(radiusField, 1, 1);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Platform.runLater(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!nameField.getText().isEmpty() && !radiusField.getText().isEmpty()),
        nameField.textProperty(), radiusField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType == ButtonType.OK) {
        List<String> data = new ArrayList<>();
        Collections.addAll(data,
            nameField.getText(),
            radiusField.getText());
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
    return "CSG " + nameField.getText() + " = new Sphere(" + radiusField.getText() + ").toCSG();";
  }
}
