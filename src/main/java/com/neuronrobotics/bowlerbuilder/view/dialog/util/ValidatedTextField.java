/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog.util;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.TextField;
import javax.annotation.Nonnull;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

public class ValidatedTextField extends TextField {

  private final ValidationSupport validator;

  public ValidatedTextField(
      @Nonnull final String invalidMessage, @Nonnull final Function<String, Boolean> isValid) {
    super();

    validator = new ValidationSupport();
    getStylesheets()
        .add(
            ValidatedTextField.class
                .getResource("/com/neuronrobotics/bowlerbuilder/styles.css")
                .toExternalForm());
    validator.setValidationDecorator(
        new StyleClassValidationDecoration("text-field-error", "text-field-warning"));
    validator.registerValidator(
        this,
        false,
        (control, value) -> {
          if (value instanceof String) {
            return ValidationResult.fromMessageIf(
                control, invalidMessage, Severity.ERROR, !isValid.apply((String) value));
          }

          return ValidationResult.fromMessageIf(control, invalidMessage, Severity.ERROR, false);
        });
    validator.redecorate();
  }

  public Boolean isInvalid() {
    return validator.isInvalid();
  }

  public ReadOnlyBooleanProperty invalidProperty() {
    return validator.invalidProperty();
  }
}
