/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.AceCreatureLabController;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine.BowlerCadEngine;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditorView;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * {@link AbstractCadEditorTab} that uses an {@link AceEditorView} and a {@link BowlerCadEngine}.
 */
@ParametersAreNonnullByDefault
public class CreatureLabTab extends AbstractCadEditorTab<AceCreatureLabController> {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(CreatureLabTab.class.getSimpleName());
  private final AceCreatureLabController controller;
  private final SplitPane pane;

  /**
   * A {@link AbstractCadEditorTab} that uses an {@link AceCreatureLabController} and a {@link
   * BowlerCadEngine}.
   *
   * @param title tab title
   * @throws IOException when loading the FXML resources
   */
  public CreatureLabTab(final String title) throws IOException {
    super(
        title,
        BowlerBuilder.getInjector().getInstance(AceEditorView.class),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine.class));

    final TabPane tabPane = new TabPane();

    final FXMLLoader cadViewerLoader =
        new FXMLLoader(
            CreatureLabTab.class.getResource(
                "/com/neuronrobotics/bowlerbuilder/view/CADModelViewer.fxml"),
            null,
            null,
            BowlerBuilder.getInjector().createChildInjector(new CADModelViewerControllerModule())
                ::getInstance);

    LOGGER.info("Loading CADModelViewer.");
    final Node cadViewer = cadViewerLoader.load();

    final FXMLLoader creatureEditorLoader =
        new FXMLLoader(
            CreatureLabTab.class.getResource(
                "/com/neuronrobotics/bowlerbuilder/view/CreatureEditor.fxml"),
            null,
            null,
            BowlerBuilder.getInjector()::getInstance);

    LOGGER.info("Loading CreatureEditor.");
    final Node creatureEditor = creatureEditorLoader.load();

    pane = new SplitPane(tabPane, creatureEditor, cadViewer);
    pane.setDividerPositions(0.2, 0.7);

    controller =
        new AceCreatureLabController(
            tabPane,
            () ->
                new FXMLLoader(
                    CreatureLabTab.class.getResource(
                        "/com/neuronrobotics/bowlerbuilder/view/AceScriptEditor.fxml"),
                    null,
                    null,
                    BowlerBuilder.getInjector()
                            .createChildInjector(
                                new AceCadEditorControllerModule(
                                    BowlerBuilder.getInjector().getInstance(AceEditorView.class)))
                        ::getInstance),
            cadViewerLoader.getController(),
            creatureEditorLoader.getController());
    this.setContent(pane);
  }

  @Override
  public Node getView() {
    return pane;
  }

  @Override
  public ScriptEditorView getScriptEditorView() {
    throw new UnsupportedOperationException(
        "CreatureLabTab does not have just one script editor"
            + " and therefore does not support getting the ScriptEditorView.");
  }

  @Override
  public AceCreatureLabController getController() {
    return controller;
  }
}
