package com.neuronrobotics.bowlerbuilder.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class MainWindowController implements Initializable {

  @FXML
  private AnchorPane root;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    root.getChildren().add(new Button("Hi!"));
  }

}
