package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.controller.AceCadEditorController;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerCadEngine;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditorView;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 * {@link CadEditorTab} that uses an
 * {@link AceEditorView} and a
 * {@link BowlerCadEngine}.
 */
public class AceCadEditorTab extends CadEditorTab<AceCadEditorController> {

  private final AceCadEditorController controller;
  private final Node node;

  public AceCadEditorTab(String title) throws IOException {
    super(
        title,
        BowlerBuilder.getInjector().getInstance(AceEditorView.class),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine.class));

    FXMLLoader loader = new FXMLLoader(AceCadEditorTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/AceCadEditor.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new AceCadEditorControllerModule(getScriptEditorView()))::getInstance);

    node = loader.load();
    controller = loader.getController();
    this.setContent(node);
  }

  @Override
  public Node getView() {
    return node;
  }

  @Override
  public AceCadEditorController getController() {
    return controller;
  }

}
