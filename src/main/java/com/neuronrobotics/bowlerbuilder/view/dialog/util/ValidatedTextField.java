package com.neuronrobotics.bowlerbuilder.view.dialog.util;

import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

public class ValidatedTextField extends TextField {

  private final BooleanProperty invalidProperty;

  public ValidatedTextField(String invalidMessage, Function<String, Boolean> isValid) {
    super();

    invalidProperty = new SimpleBooleanProperty(false);

    ValidationSupport validator = new ValidationSupport();
    getStylesheets().add(
        ValidatedTextField.class.getResource("/com/neuronrobotics/bowlerbuilder/styles.css")
            .toExternalForm());
    validator.setValidationDecorator(new StyleClassValidationDecoration("text-field-error",
        "text-field-warning"));
    validator.registerValidator(this, false, (control, value) -> {
      if (value instanceof String) {
        return ValidationResult.fromMessageIf(
            control,
            invalidMessage,
            Severity.ERROR,
            !isValid.apply((String) value));
      }

      return ValidationResult.fromMessageIf(
          control,
          invalidMessage,
          Severity.ERROR,
          false);
    });

    invalidProperty.bind(validator.invalidProperty());
  }

  public boolean isInvalid() {
    return invalidProperty.get();
  }

  public BooleanProperty invalidProperty() {
    return invalidProperty;
  }

}
