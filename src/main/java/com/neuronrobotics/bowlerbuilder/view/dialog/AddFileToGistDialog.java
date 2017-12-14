package com.neuronrobotics.bowlerbuilder.view.dialog;

import java.util.Optional;
import javafx.application.Platform;
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

    ValidationSupport validator = new ValidationSupport();
    validator.setValidationDecorator(new StyleClassValidationDecoration("text-field-error", "text-field-warning"));
    validator.registerValidator(nameField, false, (control, value) -> {
      if (value instanceof String) {
        return ValidationResult.fromMessageIf(control, "Invalid File Name", Severity.ERROR, !validateFileName((String) value).isPresent());
      }

      return ValidationResult.fromMessageIf(control, "Invalid File Name", Severity.ERROR, false);
    });

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Platform.runLater(nameField::requestFocus);

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

  /**
   * Validate a file name. A valid file name has an extension.
   * @param name File name to validate
   * @return An optional containing a valid file name, empty otherwise
   */
  private Optional<String> validateFileName(String name) {
    if (name.matches("^.*\\.[^\\\\]+$")) {
      return Optional.of(name);
    }

    return Optional.empty();
  }

  public String getName() {
    return nameField.getText();
  }

}
