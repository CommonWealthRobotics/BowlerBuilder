package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerCadEngine;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;

public class CadModelViewerControllerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CadEngine.class).to(BowlerCadEngine.class);
  }

}
