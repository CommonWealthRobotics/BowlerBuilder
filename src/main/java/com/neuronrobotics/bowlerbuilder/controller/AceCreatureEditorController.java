package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class AceCreatureEditorController {

  private static final Logger logger =
      LoggerUtilities.getLogger(AceCreatureEditorController.class.getSimpleName());
  private final TabPane scriptEditorPane;
  private final Supplier<FXMLLoader> scriptEditorSupplier;
  private final Map<String, Tab> tabNameMap;
  private final Map<Tab, AceScriptEditorController> tabControllerMap;
  private final CADModelViewerController cadModelViewerController;
  private final CreatureLabController creatureLabController;

  public AceCreatureEditorController(TabPane scriptEditorPane,
                                     Supplier<FXMLLoader> scriptEditorSupplier,
                                     CADModelViewerController cadModelViewerController,
                                     CreatureLabController creatureLabController) {
    this.scriptEditorPane = scriptEditorPane;
    this.scriptEditorSupplier = scriptEditorSupplier;
    this.tabNameMap = new HashMap<>();
    this.tabControllerMap = new HashMap<>();
    this.cadModelViewerController = cadModelViewerController;
    this.creatureLabController = creatureLabController;
  }

  public void loadFileIntoNewTab(String title, File file) {
    loadFileIntoNewTab(title, Optional.empty(), file);
  }

  public void loadFileIntoNewTab(String title, Node graphic, File file) {
    loadFileIntoNewTab(title, Optional.of(graphic), file);
  }

  private void loadFileIntoNewTab(String title, Optional<Node> graphic, File file) {
    if (tabNameMap.containsKey(title)) {
      Tab tab = tabNameMap.get(title);
      tabControllerMap.get(tab).loadFile(file);
      scriptEditorPane.getSelectionModel().select(tab);
    } else {
      Tab tab = new Tab();
      tab.setText(title);
      graphic.ifPresent(tab::setGraphic);
      scriptEditorPane.getTabs().add(tab);
      scriptEditorPane.getSelectionModel().select(tab);
      tabNameMap.put(title, tab);

      FXMLLoader loader = scriptEditorSupplier.get();
      try {
        tab.setContent(loader.load());
      } catch (IOException e) {
        logger.severe("Could not load Ace script editor.\n"
            + Throwables.getStackTraceAsString(e));
      }

      AceScriptEditorController controller = loader.getController();
      tabControllerMap.put(tab, controller);
      controller.loadFile(file);
    }
  }

  public CADModelViewerController getCadModelViewerController() {
    return cadModelViewerController;
  }

  public CreatureLabController getCreatureLabController() {
    return creatureLabController;
  }

}
