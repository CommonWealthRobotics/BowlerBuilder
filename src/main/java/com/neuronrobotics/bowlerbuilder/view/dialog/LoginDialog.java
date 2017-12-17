package com.neuronrobotics.bowlerbuilder.view.dialog;

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

    setTitle("Login to GitHub");
    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    setResultConverter(buttonType -> !buttonType.getButtonData().isCancelButton());
  }

  public String getName() {
    return nameField.getText();
  }

  public String getPassword() {
    return passField.getText();
  }

}
