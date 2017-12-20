package com.neuronrobotics.bowlerbuilder.controller;

import java.util.Locale;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.controlsfx.glyphfont.Glyph;

public class WebBrowserController {

  @FXML
  private Button backPageButton;
  @FXML
  private Button nextPageButton;
  @FXML
  private Button reloadPageButton;
  @FXML
  private Button homePageButton;
  @FXML
  private TextField urlField;
  @FXML
  private WebView webView;

  @FXML
  protected void initialize() {
    backPageButton.setGraphic(new Glyph("FontAwesome", "ARROW_LEFT"));
    nextPageButton.setGraphic(new Glyph("FontAwesome", "ARROW_RIGHT"));
    reloadPageButton.setGraphic(new Glyph("FontAwesome", "REFRESH"));
    homePageButton.setGraphic(new Glyph("FontAwesome", "HOME"));

    //Update the url field when a new page gets loaded
    webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) ->
        urlField.setText(newValue));
  }


  @FXML
  private void onBackPage(ActionEvent actionEvent) {
    Platform.runLater(() -> webView.getEngine().executeScript("history.back()"));
  }

  @FXML
  private void onNextPage(ActionEvent actionEvent) {
    Platform.runLater(() -> webView.getEngine().executeScript("history.forward()"));
  }

  @FXML
  private void onReloadPage(ActionEvent actionEvent) {
    webView.getEngine().reload();
  }

  @FXML
  private void onHomePage(ActionEvent actionEvent) {
    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");
  }

  @FXML
  private void onNavigate(ActionEvent actionEvent) {
    String url = urlField.getText();

    if (!url.toLowerCase(Locale.ENGLISH).matches("^\\w+://.*")) {
      url = String.format("http://%s", url);
    }

    loadPage(url);
  }

  public void loadPage(String url) {
    webView.getEngine().load(url);
  }

}
