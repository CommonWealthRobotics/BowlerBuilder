package com.neuronrobotics.bowlerbuilder.view.tab;

import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.controller.AceCadEditorTabController;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerCadEngine;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditorView;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javax.annotation.Nonnull;

/**
 * {@link AbstractCadEditorTab} that uses an {@link AceEditorView} and a {@link BowlerCadEngine}.
 */
public class AceCadEditorTab extends AbstractCadEditorTab<AceCadEditorTabController> {

  private final AceCadEditorTabController controller;
  private final SplitPane pane;

  public AceCadEditorTab(@Nonnull final String title) throws IOException {
    super(
        title,
        BowlerBuilder.getInjector().getInstance(AceEditorView.class),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine.class));

    final FXMLLoader scriptEditorLoader = new FXMLLoader(AceCadEditorTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/AceScriptEditor.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new AceCadEditorControllerModule(getScriptEditorView()))::getInstance);

    final Node scriptEditor = scriptEditorLoader.load();

    final FXMLLoader cadViewerLoader = new FXMLLoader(AceCadEditorTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CADModelViewer.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new CADModelViewerControllerModule())::getInstance);

    final Node cadViewer = cadViewerLoader.load();

    pane = new SplitPane(scriptEditor, cadViewer);

    controller = new AceCadEditorTabController(scriptEditorLoader.getController(),
        cadViewerLoader.getController());
    this.setContent(pane);
  }

  @Override
  public Node getView() {
    return pane;
  }

  @Override
  public AceCadEditorTabController getController() {
    return controller;
  }

}
