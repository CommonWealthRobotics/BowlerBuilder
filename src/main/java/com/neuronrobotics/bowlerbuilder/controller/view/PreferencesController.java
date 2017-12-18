package com.neuronrobotics.bowlerbuilder.controller.view;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import java.util.concurrent.ConcurrentHashMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class PreferencesController implements Initializable {

  @FXML
  private AnchorPane root;
  @FXML
  private TextField fontSizeTextField;
  private Map<String, Object> prefs; //Starting preferences

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    //Nothing to initialize yet
  }

  /**
   * Load in the current preferences and set the fields.
   *
   * @param map Current preferences
   */
  public void setPreferences(Map<String, Object> map) {
    prefs = map;
    fontSizeTextField.setText(String.valueOf(map.get("Font Size")));
  }

  /**
   * Get the changed preferences.
   *
   * @return Changed preferences
   */
  public Map<String, Object> getChanges() {
    Map<String, Object> map = new ConcurrentHashMap<>();
    Integer newFontSize = Integer.parseInt(fontSizeTextField.getText());
    if (!prefs.get("Font Size").equals(newFontSize)) {
      map.put("Font Size", newFontSize);
    }
    return map;
  }

  /**
   * Get the entire preferences.
   *
   * @return Full preferences
   */
  public Map<String, Object> getPreferences() {
    Map<String, Object> map = new ConcurrentHashMap<>();
    map.put("Font Size", Integer.parseInt(fontSizeTextField.getText()));
    return map;
  }

}
