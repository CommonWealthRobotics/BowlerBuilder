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

public class AceCreatureLabController {

  private static final Logger logger =
      LoggerUtilities.getLogger(AceCreatureLabController.class.getSimpleName());
  private final TabPane scriptEditorPane;
  private final Supplier<FXMLLoader> scriptEditorSupplier;
  private final Map<String, Tab> tabNameMap;
  private final Map<Tab, AceScriptEditorController> tabControllerMap;
  private final CADModelViewerController cadModelViewerController;
  private final CreatureEditorController creatureEditorController;

  public AceCreatureLabController(final TabPane scriptEditorPane,
      final Supplier<FXMLLoader> scriptEditorSupplier,
      final CADModelViewerController cadModelViewerController,
      final CreatureEditorController creatureEditorController) {
    this.scriptEditorPane = scriptEditorPane;
    this.scriptEditorSupplier = scriptEditorSupplier;
    this.tabNameMap = new HashMap<>();
    this.tabControllerMap = new HashMap<>();
    this.cadModelViewerController = cadModelViewerController;
    this.creatureEditorController = creatureEditorController;
  }

  public void loadFileIntoNewTab(final String title, final String pushURL, final String fileName,
      final File file) {
    loadFileIntoNewTab(title, Optional.empty(), pushURL, fileName, file);
  }

  public void loadFileIntoNewTab(
      final String title, final Node graphic, final String pushURL, final String fileName,
      final File file) {
    loadFileIntoNewTab(title, Optional.of(graphic), pushURL, fileName, file);
  }

  private void loadFileIntoNewTab(final String title, final Optional<Node> graphic,
      final String pushURL,
      final String fileName, final File file) {
    if (tabNameMap.containsKey(title)) {
      final Tab tab = tabNameMap.get(title);
      if (tabControllerMap.containsKey(tab)) {
        tabControllerMap.get(tab).loadManualGist(pushURL, fileName, file);
        scriptEditorPane.getSelectionModel().select(tab);
      }
    } else {
      final Tab tab = new Tab();
      tab.setText(title);
      graphic.ifPresent(tab::setGraphic);
      tab.setOnClosed(event -> {
        tabNameMap.remove(title);
        tabControllerMap.remove(tab);
      });

      scriptEditorPane.getTabs().add(tab);
      scriptEditorPane.getSelectionModel().select(tab);
      tabNameMap.put(title, tab);

      final FXMLLoader loader = scriptEditorSupplier.get();
      try {
        tab.setContent(loader.load());
      } catch (final IOException e) {
        logger.severe("Could not load Ace script editor.\n"
            + Throwables.getStackTraceAsString(e));
      }

      final AceScriptEditorController controller = loader.getController();
      tabControllerMap.put(tab, controller);
      controller.loadManualGist(pushURL, fileName, file);
    }
  }

  public CADModelViewerController getCadModelViewerController() {
    return cadModelViewerController;
  }

  public CreatureEditorController getCreatureEditorController() {
    return creatureEditorController;
  }

}
