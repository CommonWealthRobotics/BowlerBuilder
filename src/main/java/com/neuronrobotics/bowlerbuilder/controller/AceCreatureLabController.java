/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
import javax.annotation.Nonnull;

public class AceCreatureLabController {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(AceCreatureLabController.class.getSimpleName());
  private final TabPane scriptEditorPane;
  private final Supplier<FXMLLoader> scriptEditorSupplier;
  private final Map<File, Tab> tabMap;
  private final Map<Tab, AceScriptEditorController> tabControllerMap;
  private final CADModelViewerController cadModelViewerController;
  private final CreatureEditorController creatureEditorController;

  public AceCreatureLabController(@Nonnull final TabPane scriptEditorPane,
      @Nonnull final Supplier<FXMLLoader> scriptEditorSupplier,
      @Nonnull final CADModelViewerController cadModelViewerController,
      @Nonnull final CreatureEditorController creatureEditorController) {
    this.scriptEditorPane = scriptEditorPane;
    this.scriptEditorSupplier = scriptEditorSupplier;
    this.tabMap = new HashMap<>();
    this.tabControllerMap = new HashMap<>();
    this.cadModelViewerController = cadModelViewerController;
    this.creatureEditorController = creatureEditorController;
  }

  public void loadFileIntoNewTab(@Nonnull final String title, @Nonnull final String pushURL,
      @Nonnull final String fileName, @Nonnull final File file) {
    loadFileIntoNewTab(title, Optional.empty(), pushURL, fileName, file);
  }

  public void loadFileIntoNewTab(@Nonnull final String title, @Nonnull final Node graphic,
      @Nonnull final String pushURL, @Nonnull final String fileName, @Nonnull final File file) {
    loadFileIntoNewTab(title, Optional.of(graphic), pushURL, fileName, file);
  }

  private void loadFileIntoNewTab(@Nonnull final String title,
      @Nonnull final Optional<Node> graphic, @Nonnull final String pushURL,
      @Nonnull final String fileName, @Nonnull final File file) {
    if (tabMap.containsKey(file)) {
      final Tab tab = tabMap.get(file);
      if (tabControllerMap.containsKey(tab)) {
        tabControllerMap.get(tab).loadManualGist(pushURL, fileName, file);
        scriptEditorPane.getSelectionModel().select(tab);
      }
    } else {
      final Tab tab = new Tab();
      tab.setText(title);
      graphic.ifPresent(tab::setGraphic);
      tab.setOnClosed(event -> {
        tabMap.remove(file);
        tabControllerMap.remove(tab);
      });

      scriptEditorPane.getTabs().add(tab);
      scriptEditorPane.getSelectionModel().select(tab);
      tabMap.put(file, tab);

      final FXMLLoader loader = scriptEditorSupplier.get();
      try {
        tab.setContent(loader.load());
      } catch (final IOException e) {
        LOGGER.severe("Could not load Ace script editor.\n"
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
