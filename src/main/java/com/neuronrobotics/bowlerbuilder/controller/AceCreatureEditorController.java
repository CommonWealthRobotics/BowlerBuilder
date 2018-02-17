package com.neuronrobotics.bowlerbuilder.controller;

public class AceCreatureEditorController {

  private final AceScriptEditorController aceScriptEditorController;
  private final CADModelViewerController cadModelViewerController;
  private final CreatureLabController creatureLabController;

  public AceCreatureEditorController(AceScriptEditorController aceScriptEditorController,
                                     CADModelViewerController cadModelViewerController,
                                     CreatureLabController creatureLabController) {
    this.aceScriptEditorController = aceScriptEditorController;
    this.cadModelViewerController = cadModelViewerController;
    this.creatureLabController = creatureLabController;
  }

  public AceScriptEditorController getAceScriptEditorController() {
    return aceScriptEditorController;
  }

  public CADModelViewerController getCadModelViewerController() {
    return cadModelViewerController;
  }

  public CreatureLabController getCreatureLabController() {
    return creatureLabController;
  }

}
