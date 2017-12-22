package com.neuronrobotics.bowlerbuilder.controller.view;

import com.google.inject.Guice;
import com.neuronrobotics.bowlerbuilder.controller.AceCadEditorController;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerbuilder.controller.module.CadModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace.AceEditorView;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 * {@link CadEditorTab} that uses an
 * {@link AceEditorView} and a
 * {@link com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerStudio3dEngine}.
 */
public class AceCadEditorTab extends CadEditorTab<AceCadEditorController> {

  private final AceCadEditorController controller;
  private final Node node;

  public AceCadEditorTab(String title)
      throws IOException {
    super(title, new AceEditorView(), new BowlerStudio3dEngine());

    FXMLLoader loader = new FXMLLoader(AceCadEditorTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/AceCadEditor.fxml"),
        null,
        null,
        Guice.createInjector(
            new AceCadEditorControllerModule(getScriptEditorView()),
            new CadModelViewerControllerModule())::getInstance);

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
