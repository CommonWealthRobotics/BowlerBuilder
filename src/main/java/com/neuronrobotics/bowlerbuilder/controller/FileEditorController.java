package com.neuronrobotics.bowlerbuilder.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

public class FileEditorController implements Initializable {

  @FXML
  private TextArea textArea;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    //Nothing to initialize yet
  }

  public void setInitialText(String text) {
    textArea.setText(text);
  }

}
