package com.neuronrobotics.bowlerbuilder.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class FileEditorController implements Initializable {

  @FXML
  private AnchorPane editorRoot;
  @FXML
  private WebView webView;
  private WebEngine webEngine; //NOPMD

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    webEngine = webView.getEngine();
    webEngine.setJavaScriptEnabled(true);
    webEngine.load(getClass().getResource("../web/ace.html").toString());
  }

}
