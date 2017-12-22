package com.neuronrobotics.bowlerbuilder.controller.view;

import com.google.inject.Guice;
import com.neuronrobotics.bowlerbuilder.controller.FileEditorController;
import com.neuronrobotics.bowlerbuilder.controller.FileEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class FileEditorTab extends EditorTab<FileEditorController> {

  private final FileEditorController controller;
  private final Node node;

  public FileEditorTab(String title, String fxmlFile, ScriptEditorView scriptEditorView)
      throws IOException {
    super(title, scriptEditorView.getScriptEditor());

    FXMLLoader loader = new FXMLLoader(FileEditorTab.class.getResource(fxmlFile),
        null,
        null,
        Guice.createInjector(new FileEditorControllerModule())::getInstance);
    node = loader.load();
    controller = loader.getController();
    this.setContent(node);
  }

  @Override
  public Node getView() {
    return node;
  }

  @Override
  public FileEditorController getController() {
    return controller;
  }

}
