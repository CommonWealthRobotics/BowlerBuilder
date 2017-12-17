package com.neuronrobotics.bowlerbuilder.view.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginDialog extends Dialog<Boolean> {

  private TextField nameField;
  private PasswordField passField;

  public LoginDialog() {
    super();

    VBox vBox = new VBox();
    nameField = new TextField();
    passField = new PasswordField();

    nameField.setPromptText("Username");
    passField.setPromptText("Password");

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
