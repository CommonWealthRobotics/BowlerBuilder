package com.neuronrobotics.bowlerbuilder.controller.cadengine;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.util.CsgParser;

public class BowlerCadEngineFactory {

  public BowlerCadEngine create(CsgParser csgParser) {
    return new BowlerCadEngine(csgParser);
  }

}
