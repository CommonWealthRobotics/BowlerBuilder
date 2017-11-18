package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.aceEditor.AceEditor;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewCubeDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewCylinderDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewRoundedCubeDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewSphereDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
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

public class FileEditorController implements Initializable {

  @FXML
  private SplitPane root;
  @FXML
  private WebView webView;
  private WebEngine webEngine; //NOPMD
  private AceEditor aceEditor;
  @FXML
  private Button runButton;
  @FXML
  private Button publishButton;
  @FXML
  private TextField fileNameField;
  @FXML
  private TextField gistNameField;

  private int requestedFontSize;
  private Optional<File> requestedFile;

  public FileEditorController() {
    requestedFontSize = 14; //TODO: Load previous font size preference
    requestedFile = Optional.empty();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    root.setDividerPosition(0, 1.0);
    webEngine = webView.getEngine();
    webEngine.setJavaScriptEnabled(true);
    webEngine.load(getClass().getResource("../web/ace.html").toString());
    aceEditor = new AceEditor(webEngine);

    runButton.setGraphic(new FontAwesome().create(String.valueOf(FontAwesome.Glyph.PLAY)));
    publishButton.setGraphic(
        new FontAwesome().create(String.valueOf(FontAwesome.Glyph.CLOUD_UPLOAD)));

    //Stuff to run once the engine is done loading
    webEngine.getLoadWorker().stateProperty().addListener(
        (ObservableValue<? extends Worker.State> observable,
         Worker.State oldValue,
         Worker.State newValue) -> {
          if (newValue == Worker.State.SUCCEEDED) {
            aceEditor.setFontSize(requestedFontSize); //Set font size to the default
            requestedFile.ifPresent(file -> aceEditor.insertAtCursor(file.getAbsolutePath()));
          }
        });
  }

  @FXML
  private void runFile(ActionEvent actionEvent) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/CADModelViewer.fxml"));
    try {
      root.getItems().add(loader.load());
      CADModelViewerController controller = loader.getController();
      root.setDividerPosition(0, 0.8);

      Object result = ScriptingEngine.inlineScriptStringRun(aceEditor.getText(),
          new ArrayList<>(),
          "Groovy");

      if (result instanceof CSG) {
        controller.addMeshesFromCSG((CSG) result);
      } else if (result instanceof List) {
        List casted = (List) result;
        casted.forEach(elem -> {
          if (elem instanceof CSG) {
            controller.addMeshesFromCSG((CSG) elem);
          }
        });
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load CADModelViewer.\n" + Throwables.getStackTraceAsString(e));
    } catch (Exception e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not run CAD script.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void publishFile(ActionEvent actionEvent) {
    //TODO: GitHub integration & publish changes to gist
  }

  @FXML
  private void newCube(ActionEvent actionEvent) {
    NewCubeDialog dialog = new NewCubeDialog();

    if (dialog.showAndWait().isPresent()) {
      aceEditor.insertAtCursor(dialog.getResultAsScript());
    }

    Platform.runLater(webView::requestFocus);
  }

  @FXML
  private void newRoundedCube(ActionEvent actionEvent) {
    NewRoundedCubeDialog dialog = new NewRoundedCubeDialog();

    if (dialog.showAndWait().isPresent()) {
      aceEditor.insertAtCursor(dialog.getResultAsScript());
    }

    Platform.runLater(webView::requestFocus);
  }

  @FXML
  private void newSphere(ActionEvent actionEvent) {
    NewSphereDialog dialog = new NewSphereDialog();

    if (dialog.showAndWait().isPresent()) {
      aceEditor.insertAtCursor(dialog.getResultAsScript());
    }

    Platform.runLater(webView::requestFocus);
  }

  @FXML
  private void newCylinder(ActionEvent actionEvent) {
    NewCylinderDialog dialog = new NewCylinderDialog();

    if (dialog.showAndWait().isPresent()) {
      aceEditor.insertAtCursor(dialog.getResultAsScript());
    }

    Platform.runLater(webView::requestFocus);
  }

  /**
   * Set the font size of this editor.
   *
   * @param fontSize Font size
   */
  public void setFontSize(int fontSize) {
    if (webEngine.getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
      aceEditor.setFontSize(fontSize);
    } else {
      requestedFontSize = fontSize;
    }
  }

  /**
   * Load a file from disk and insert its content into the editor.
   *
   * @param file File to load
   */
  public void loadFile(File file) {
    if (webEngine.getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
      aceEditor.insertAtCursor(file.getAbsolutePath());
    } else {
      requestedFile = Optional.of(file);
    }
  }

}
