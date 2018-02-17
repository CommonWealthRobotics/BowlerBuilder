package com.neuronrobotics.bowlerbuilder.view.dialog.util;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.TextField;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

public class ValidatedTextField extends TextField {

  private final ValidationSupport validator;

  public ValidatedTextField(String invalidMessage, Function<String, Boolean> isValid) {
    super();

    validator = new ValidationSupport();
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
  }

  public Boolean isInvalid() {
    return validator.isInvalid();
  }

  public ReadOnlyBooleanProperty invalidProperty() {
    return validator.invalidProperty();
  }

}
