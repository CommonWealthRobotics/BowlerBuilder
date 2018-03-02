package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import javafx.scene.web.WebEngine;
import javax.annotation.Nonnull;

public class AceWebEngineFactory {

  public AceWebEngine create(@Nonnull final WebEngine webEngine) {
    return new AceWebEngine(webEngine);
  }

}
