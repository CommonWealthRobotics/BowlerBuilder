/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.dialog.plugin;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AddPluginDialog extends Dialog<Boolean> {

  private final TextField sourceField;
  private final TextField displayNameField;

  public AddPluginDialog() {
    super();

    sourceField = new TextField();
    sourceField.setId("sourceField");
    sourceField.setPromptText("Gist URL");

    displayNameField = new TextField();
    displayNameField.setId("displayNameField");
    displayNameField.setPromptText("Display Name");

    final VBox vBox = new VBox();
    vBox.setSpacing(5);
    vBox.getChildren().addAll(sourceField, displayNameField);
    sourceField.requestFocus();

    setTitle("Add Plugin");
    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    final Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!sourceField.getText().isEmpty()
                && !displayNameField.getText().isEmpty()),
        sourceField.textProperty(),
        displayNameField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> !buttonType.getButtonData().isCancelButton());
  }

  public String getSource() {
    return sourceField.getText();
  }

  public String getDisplayName() {
    return displayNameField.getText();
  }

}
