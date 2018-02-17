package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.controller.AceCreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerCadEngine;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditorView;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 * {@link CadEditorTab} that uses an {@link AceEditorView} and a {@link BowlerCadEngine}.
 */
public class CreatureLabTab extends CadEditorTab<AceCreatureEditorController> {

  private final AceCreatureEditorController controller;
  private final SplitPane pane;

  public CreatureLabTab(String title) throws IOException {
    super(
        title,
        BowlerBuilder.getInjector().getInstance(AceEditorView.class),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine.class));

    FXMLLoader scriptEditorLoader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/AceScriptEditor.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new AceCadEditorControllerModule(getScriptEditorView()))::getInstance);

    Node scriptEditor = scriptEditorLoader.load();

    FXMLLoader cadViewerLoader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CADModelViewer.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new CADModelViewerControllerModule())::getInstance);

    Node cadViewer = cadViewerLoader.load();

    FXMLLoader creatureEditorLoader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CreatureLab.fxml"),
        null,
        null,
        BowlerBuilder.getInjector()::getInstance);

    Node creatureEditor = creatureEditorLoader.load();

    pane = new SplitPane(scriptEditor, creatureEditor, cadViewer);

    controller = new AceCreatureEditorController(scriptEditorLoader.getController(),
        cadViewerLoader.getController(), creatureEditorLoader.getController());
    this.setContent(pane);
  }

  @Override
  public Node getView() {
    return pane;
  }

  @Override
  public AceCreatureEditorController getController() {
    return controller;
  }

}
