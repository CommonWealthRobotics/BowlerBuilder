package com.neuronrobotics.bowlerbuilder.controller;

import javafx.scene.web.WebEngine;

public final class AceInterface {

  private final WebEngine engine;

  public AceInterface(WebEngine engine) {
    this.engine = engine;
  }

  /**
   * Insert text at the cursor position.
   *
   * @param text Text to insert
   */
  public void insertAtCursor(String text) {
    engine.executeScript("editor.insert(\"" + text + "\");");
  }

}
