/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NewGistDialog extends Dialog<List<String>> {

  private final ValidatedTextField nameField;
  private final TextField descField;
  private final CheckBox publicBox;

  /** A {@link Dialog} to make a new gist. */
  public NewGistDialog() {
    super();

    nameField =
        new ValidatedTextField(
            "Invalid File Name", name -> GistUtilities.isValidCodeFileName(name).isPresent());
    descField = new TextField();
    publicBox = new CheckBox();

    nameField.setId("nameField");
    descField.setId("descField");
    publicBox.setId("publicBox");

    publicBox.setSelected(true);

    setTitle("New Gist");

    final GridPane pane = new GridPane();
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

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Platform.runLater(nameField::requestFocus);

    final Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton
        .disableProperty()
        .bind(
            Bindings.createBooleanBinding(
                () -> !(!nameField.invalidProperty().getValue() && !descField.getText().isEmpty()),
                nameField.invalidProperty(),
                descField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(
        buttonType -> {
          if (buttonType.equals(ButtonType.OK)) {
            final List<String> data = new ArrayList<>();
            Collections.addAll(data, nameField.getText(), descField.getText());
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

  public boolean isPublic() {
    return publicBox.isSelected();
  }

  public boolean isInvalidName() {
    return nameField.invalidProperty().get();
  }

  public ReadOnlyBooleanProperty invalidNameProperty() {
    return nameField.invalidProperty();
  }
}
