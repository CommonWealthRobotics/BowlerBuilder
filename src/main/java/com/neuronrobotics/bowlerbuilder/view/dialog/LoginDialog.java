/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.dialog;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginDialog extends Dialog<Boolean> {

  private final TextField nameField;
  private final PasswordField passField;

  public LoginDialog() {
    super();

    nameField = new TextField();
    nameField.setId("loginNameField");
    nameField.setPromptText("Username");

    passField = new PasswordField();
    passField.setId("loginPassField");
    passField.setPromptText("Password");

    VBox vBox = new VBox();
    vBox.setSpacing(5);
    vBox.getChildren().addAll(nameField, passField);
    nameField.requestFocus();

    setTitle("Login to GitHub");
    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!nameField.getText().isEmpty()
                && !passField.getText().isEmpty()),
        nameField.textProperty(),
        passField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> !buttonType.getButtonData().isCancelButton());
  }

  public String getName() {
    return nameField.getText();
  }

  public String getPassword() {
    return passField.getText();
  }

}
