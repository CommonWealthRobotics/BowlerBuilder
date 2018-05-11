/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AddFileToGistDialog extends Dialog<String> {

  private final ValidatedTextField nameField;

  /** A {@link Dialog} to upload a file to a GitHub Gist. */
  public AddFileToGistDialog() {
    super();

    nameField =
        new ValidatedTextField(
            "Invalid File Name", name -> GistUtilities.isValidCodeFileName(name).isPresent());
    nameField.setId("nameField");

    setTitle("New File");

    final GridPane pane = new GridPane();
    pane.setId("root");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(nameField, 1, 0);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    // FxUtil.runFX(nameField::requestFocus);

    final Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(nameField.invalidProperty());
    addButton.setDefaultButton(true);

    setResultConverter(
        buttonType -> {
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
