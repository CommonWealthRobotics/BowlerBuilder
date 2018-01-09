package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

public class NewGistDialog extends Dialog<List<String>> {

  private final TextField nameField;
  private final TextField descField;
  private final CheckBox publicBox;
  private final BooleanProperty invalidNameProperty;

  public NewGistDialog() {
    super();

    invalidNameProperty = new SimpleBooleanProperty(false);

    nameField = new TextField();
    descField = new TextField();
    publicBox = new CheckBox();

    nameField.setId("nameField");
    descField.setId("descField");
    publicBox.setId("publicBox");

    publicBox.setSelected(true);

    setTitle("New Gist");

    GridPane pane = new GridPane();
    pane.setId("newGistRoot");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(new Label("Description"), 0, 1);
    pane.add(new Label("Public"), 0, 2);
    pane.add(nameField, 1, 0);
    pane.add(descField, 1, 1);
    pane.add(publicBox, 1, 2);

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
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!invalidNameProperty.getValue()
                && !descField.getText().isEmpty()),
        invalidNameProperty,
        descField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        List<String> data = new ArrayList<>();
        Collections.addAll(data,
            nameField.getText(),
            descField.getText());
        return data;
      }
      return null;
    });
  }

  public String getName() {
    return nameField.getText();
  }

  public String getDescription() {
    return descField.getText();
  }

  /**
   * Return whether the gist should be public.
   *
   * @return Whether the gist should be public
   */
  public boolean getIsPublic() {
    return publicBox.isSelected();
  }

  public boolean isInvalidName() {
    return invalidNameProperty.get();
  }

  public BooleanProperty invalidNameProperty() {
    return invalidNameProperty;
  }

}

