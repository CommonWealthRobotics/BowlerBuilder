package com.neuronrobotics.bowlerbuilder.view.dialog;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AddFileToGistDialog extends Dialog<String> {

  private final TextField nameField;

  public AddFileToGistDialog() {
    super();

    nameField = new TextField();

    nameField.setId("nameField");

    setTitle("New File");

    GridPane pane = new GridPane();
    pane.setId("newGistRoot");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(nameField, 1, 0);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Platform.runLater(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
        nameField.getText().isEmpty(), nameField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        return nameField.getText();
      }
      return null;
    });
  }

  public String getName() {
    return nameField.getText();
  }

}
