package com.neuronrobotics.bowlerbuilder.controller.view;

import com.neuronrobotics.bowlerbuilder.controller.FileEditorController;
import javafx.scene.control.Tab;

public class FileEditorTab extends Tab {

  private final FileEditorController controller;

  public FileEditorTab(String text, FileEditorController controller) {
    super(text);
    this.controller = controller;
  }

  public FileEditorController getController() {
    return controller;
  }

}
