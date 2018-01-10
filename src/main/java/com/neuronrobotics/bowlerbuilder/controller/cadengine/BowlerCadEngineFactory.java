package com.neuronrobotics.bowlerbuilder.controller.cadengine;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.util.CsgParser;
import javafx.scene.control.ProgressIndicator;

public class BowlerCadEngineFactory {

  public BowlerCadEngine create(CsgParser csgParser, ProgressIndicator progressIndicator) {
    return new BowlerCadEngine(csgParser, progressIndicator);
  }

}
