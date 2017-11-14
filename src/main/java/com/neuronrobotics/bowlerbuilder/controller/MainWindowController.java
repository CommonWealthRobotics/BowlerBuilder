package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.controlsfx.control.PropertySheet;

public class MainWindowController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private TabPane tabPane;
  @FXML
  private TextArea console;

  //Open file editors
  private List<FileEditorController> fileEditors;

  private static Map<String, Object> preferences;

  public MainWindowController() {
    fileEditors = new ArrayList<>();
    preferences = new HashMap<>();
    preferences.put("Font Size", 14);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    console.setText(console.getText() + new SimpleDateFormat("HH:mm:ss, MM dd, yyyy",
        new Locale("en", "US")).format(new Date()));
  }

  @FXML
  private void openNewCADFile(ActionEvent actionEvent) {
    Tab tab = new Tab("New file");
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/FileEditor.fxml"));
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
    ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
    for (String key : preferences.keySet()) {
      list.add(new CustomPropertyItem(key));
    }
    PropertySheet sheet = new PropertySheet(list);
    Dialog<PropertySheet> dialog = new Dialog<>();
    dialog.getDialogPane().setContent(sheet);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
    dialog.showAndWait();
  }

  class CustomPropertyItem implements PropertySheet.Item {
    private String key;
    private String category, name;

    public CustomPropertyItem(String key) {
      this.key = key;
      String[] skey = key.split("#");
      category = skey[0];
      name = skey[0];
    }

    @Override
    public Class<?> getType() {
      return preferences.get(key).getClass();
    }

    @Override
    public String getCategory() {
      return category;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Object getValue() {
      return preferences.get(key);
    }

    @Override
    public void setValue(Object value) {
      preferences.put(key, value);
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
      return Optional.empty();
    }
  }

}
