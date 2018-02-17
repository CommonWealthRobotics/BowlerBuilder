package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.AceCreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerCadEngine;
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

/**
 * {@link CadEditorTab} that uses an {@link AceEditorView} and a {@link BowlerCadEngine}.
 */
public class CreatureLabTab extends CadEditorTab<AceCreatureEditorController> {

  private static final Logger logger =
      LoggerUtilities.getLogger(CreatureLabTab.class.getSimpleName());
  private final AceCreatureEditorController controller;
  private final SplitPane pane;

  public CreatureLabTab(String title) throws IOException {
    super(
        title,
        BowlerBuilder.getInjector().getInstance(AceEditorView.class),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine.class));

    final TabPane tabPane = new TabPane();

    FXMLLoader cadViewerLoader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CADModelViewer.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new CADModelViewerControllerModule())::getInstance);

    Node cadViewer = cadViewerLoader.load();

    FXMLLoader creatureEditorLoader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CreatureEditor.fxml"),
        null,
        null,
        BowlerBuilder.getInjector()::getInstance);

    Node creatureEditor = creatureEditorLoader.load();

    pane = new SplitPane(tabPane, creatureEditor, cadViewer);

    controller = new AceCreatureEditorController(tabPane,
        () -> new FXMLLoader(CreatureLabTab.class.getResource(
            "/com/neuronrobotics/bowlerbuilder/view/AceScriptEditor.fxml"),
            null,
            null,
            BowlerBuilder.getInjector().createChildInjector(
                new AceCadEditorControllerModule(BowlerBuilder.getInjector()
                    .getInstance(AceEditorView.class)))::getInstance),
        cadViewerLoader.getController(), creatureEditorLoader.getController());
    this.setContent(pane);
  }

  @Override
  public Node getView() {
    return pane;
  }

  @Override
  public ScriptEditorView getScriptEditorView() {
    throw new UnsupportedOperationException("CreatureLabTab does not have just one script editor"
        + " and therefore does not support getting the ScriptEditorView.");
  }

  @Override
  public AceCreatureEditorController getController() {
    return controller;
  }

}
