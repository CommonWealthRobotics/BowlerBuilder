package com.neuronrobotics.bowlerbuilder.controller.view;

import com.neuronrobotics.bowlerbuilder.controller.FileEditorController;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class FileEditorTab extends AbstractTab<FileEditorController> {

  private final FileEditorController controller;
  private final Node node;

  public FileEditorTab(String title, String fxmlFile) throws IOException {
    super(title);

    FXMLLoader loader = new FXMLLoader(FileEditorTab.class.getResource(fxmlFile));
    node = loader.load();
    controller = loader.getController();
    this.setContent(node);
  }

  @Override
  public Node getRoot() {
    return node;
  }

  @Override
  public FileEditorController getController() {
    return controller;
  }

}
