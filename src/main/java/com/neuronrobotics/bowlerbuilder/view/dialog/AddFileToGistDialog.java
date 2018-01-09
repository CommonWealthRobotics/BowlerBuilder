package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

public class AddFileToGistDialog extends Dialog<String> {

  private final TextField nameField;
  private final BooleanProperty invalidNameProperty;

  public AddFileToGistDialog() {
    super();

    invalidNameProperty = new SimpleBooleanProperty(false);

    nameField = new TextField();
    nameField.setId("nameField");

    setTitle("New File");

    GridPane pane = new GridPane();
    pane.setId("root");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(nameField, 1, 0);

    ValidationSupport validator = new ValidationSupport();
    validator.setValidationDecorator(new StyleClassValidationDecoration(
        "text-field-error",
        "text-field-warning"));
    validator.registerValidator(nameField, false, (control, value) -> {
      if (value instanceof String) {
        return ValidationResult.fromMessageIf(
            control,
            "Invalid File Name",
            Severity.ERROR,
            !GistUtilities.isValidCodeFileName((String) value).isPresent());
      }

      return ValidationResult.fromMessageIf(
          control,
          "Invalid File Name",
          Severity.ERROR,
          false);
    });

    invalidNameProperty.bind(validator.invalidProperty());

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    FxUtil.runFX(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(validator.invalidProperty());
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
    return invalidNameProperty.get();
  }

  public BooleanProperty invalidNameProperty() {
    return invalidNameProperty;
  }

}
