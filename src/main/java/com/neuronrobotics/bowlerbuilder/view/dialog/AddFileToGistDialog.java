package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class AddFileToGistDialog extends Dialog<String> {

  private final ValidatedTextField nameField;

  public AddFileToGistDialog() {
    super();

    nameField = new ValidatedTextField("Invalid File Name", name ->
        GistUtilities.isValidCodeFileName(name).isPresent());
    nameField.setId("nameField");

    setTitle("New File");

    GridPane pane = new GridPane();
    pane.setId("root");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(nameField, 1, 0);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    //FxUtil.runFX(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(nameField.invalidProperty());
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

  public boolean isInvalidName() {
    return nameField.invalidProperty().get();
  }

  public ReadOnlyBooleanProperty invalidNameProperty() {
    return nameField.invalidProperty();
  }

}
