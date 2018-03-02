package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;

/**
 * Simple passthrough to the real {@link WebEngine}.
 */
public class AceWebEngine implements WebEngineAdapter {

  private final WebEngine webEngine;

  public AceWebEngine(final WebEngine webEngine) {
    this.webEngine = webEngine;
  }

  @Override
  public Object executeScript(final String script) {
    return webEngine.executeScript(script);
  }

  @Override
  public Worker<Void> getLoadWorker() {
    return webEngine.getLoadWorker();
  }
}
