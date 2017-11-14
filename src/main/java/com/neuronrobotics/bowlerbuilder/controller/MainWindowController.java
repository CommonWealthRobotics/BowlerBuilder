package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.view.PreferencesController;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class MainWindowController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private TabPane tabPane;
  @FXML
  private TextArea console;

  //Open file editors
  private final List<FileEditorController> fileEditors;
  private Map<String, Object> preferences;

  public MainWindowController() {
    fileEditors = new ArrayList<>();
    preferences = new HashMap<>();
    preferences.put("Font Size", 14); //TODO: Load previous font size preference
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    console.setText(console.getText() + new SimpleDateFormat("HH:mm:ss, MM dd, yyyy",
        new Locale("en", "US")).format(new Date()));
  }

  @FXML
  private void openNewCADFile(ActionEvent actionEvent) {
    Tab tab = new Tab("New file");
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/FileEditor.fxml"));
    try {
      Node content = loader.load();
      final FileEditorController controller = loader.getController();
      controller.setFontSize((int) preferences.get("Font Size"));
      fileEditors.add(controller);
      tab.setContent(content);
      tab.setOnCloseRequest(event -> fileEditors.remove(controller));
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load FileEditor.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);
  }

  @FXML
  private void exitProgram(ActionEvent actionEvent) {
    root.getScene().getWindow().hide();
  }

  @FXML
  private void logInToGitHub(ActionEvent actionEvent) {
    //TODO: Use ScriptingEngine's GitHub to log in
  }

  @FXML
  private void openPreferences(ActionEvent actionEvent) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/Preferences.fxml"));
    try {
      Node content = loader.load();
      Dialog dialog = new Dialog();
      dialog.getDialogPane().setContent(content);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
      PreferencesController controller = loader.getController();
      controller.setPreferences(preferences);
      dialog.showAndWait();
      preferences = controller.getPreferences();
      if (controller.getPreferences().containsKey("Font Size")) {
        fileEditors.forEach(elem ->
            elem.setFontSize((Integer) controller.getPreferences().get("Font Size")));
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load Preferences.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

}
