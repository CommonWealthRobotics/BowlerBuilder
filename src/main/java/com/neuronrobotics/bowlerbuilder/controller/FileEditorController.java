package com.neuronrobotics.bowlerbuilder.controller;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewCubeDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewCylinderDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewRoundedCubeDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewSphereDialog;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
//import org.kohsuke.github.GitHub;

public class FileEditorController implements Initializable {

  @FXML
  private SplitPane root;
  @FXML
  private WebView webView;
  private WebEngine webEngine; //NOPMD
  @FXML
  private Button runButton;
  @FXML
  private Button publishButton;
  @FXML
  private TextField fileNameField;
  @FXML
  private TextField gistNameField;

//  private ObjectProperty<GitHub> gitHub;

  private static final Glyph FONTAWESOME_PLAY = new FontAwesome().create(FontAwesome.Glyph.PLAY);
  private static final Glyph FONTAWESOME_PAUSE = new FontAwesome().create(FontAwesome.Glyph.PAUSE);
  private static final Glyph FONTAWESOME_CLOUD_UPLOAD =
      new FontAwesome().create(FontAwesome.Glyph.CLOUD_UPLOAD);

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    root.setDividerPosition(0, 1.0);
    webEngine = webView.getEngine();
    webEngine.setJavaScriptEnabled(true);
    webEngine.load(getClass().getResource("../web/ace.html").toString());

    runButton.setGraphic(FONTAWESOME_PLAY);
    publishButton.setGraphic(FONTAWESOME_CLOUD_UPLOAD);
  }

  @FXML
  private void runFile(ActionEvent actionEvent) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/CADModelViewer.fxml"));
    try {
      root.getItems().add(loader.load());
      root.setDividerPosition(0, 0.8);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE, "Could not load CADModelViewer");
    }
  }

  @FXML
  private void publishFile(ActionEvent actionEvent) {
    //TODO: GitHub integration & publish changes to gist
  }

//  public void setGitHub(GitHub gitHub) {
//    this.gitHub.set(gitHub);
//  }

  @FXML
  private void newCube(ActionEvent actionEvent) {
    NewCubeDialog dialog = new NewCubeDialog();

    if (dialog.showAndWait().isPresent()) {
      webEngine.executeScript("editor.insert(\"" + dialog.getResultAsScript() + "\");");
    }

    Platform.runLater(webView::requestFocus);
  }

  public void newRoundedCube(ActionEvent actionEvent) {
    NewRoundedCubeDialog dialog = new NewRoundedCubeDialog();

    if (dialog.showAndWait().isPresent()) {
      webEngine.executeScript("editor.insert(\"" + dialog.getResultAsScript() + "\");");
    }

    Platform.runLater(webView::requestFocus);
  }

  public void newSphere(ActionEvent actionEvent) {
    NewSphereDialog dialog = new NewSphereDialog();

    if (dialog.showAndWait().isPresent()) {
      webEngine.executeScript("editor.insert(\"" + dialog.getResultAsScript() + "\");");
    }

    Platform.runLater(webView::requestFocus);
  }

  public void newCylinder(ActionEvent actionEvent) {
    NewCylinderDialog dialog = new NewCylinderDialog();

    if (dialog.showAndWait().isPresent()) {
      webEngine.executeScript("editor.insert(\"" + dialog.getResultAsScript() + "\");");
    }

    Platform.runLater(webView::requestFocus);
  }
}
