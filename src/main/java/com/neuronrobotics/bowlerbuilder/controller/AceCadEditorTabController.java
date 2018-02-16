package com.neuronrobotics.bowlerbuilder.controller;

import eu.mihosoft.vrl.v3d.CSG;

public class AceCadEditorTabController {

  private final AceScriptEditorController aceScriptEditorController;
  private final CADModelViewerController cadModelViewerController;

  public AceCadEditorTabController(
      AceScriptEditorController aceScriptEditorController,
      CADModelViewerController cadModelViewerController) {
    this.aceScriptEditorController = aceScriptEditorController;
    this.cadModelViewerController = cadModelViewerController;

    aceScriptEditorController.getScriptRunner().resultProperty()
        .addListener((observable, oldValue, newValue) -> parseCSG(newValue));
  }

  /**
   * Recursively parse CSG's out of an Object. Can contain CSGs in an Iterable. Adds CSG's to the
   * CAD viewer.
   *
   * @param item script result
   */
  private void parseCSG(Object item) {
    if (item instanceof CSG) {
      cadModelViewerController.addCSG((CSG) item);
    } else if (item instanceof Iterable) {
      Iterable<?> itemList = (Iterable) item;
      itemList.forEach(this::parseCSG);
    }
  }

  public AceScriptEditorController getAceScriptEditorController() {
    return aceScriptEditorController;
  }

  public CADModelViewerController getCadModelViewerController() {
    return cadModelViewerController;
  }

}
