package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;

/**
 * Simple passthrough to the real {@link WebEngine}.
 */
public class AceWebEngine implements WebEngineAdapter {

  private final WebEngine webEngine;

  public AceWebEngine(WebEngine webEngine) {
    this.webEngine = webEngine;
  }

  @Override
  public Object executeScript(String script) {
    return webEngine.executeScript(script);
  }

  @Override
  public Worker<Void> getLoadWorker() {
    return webEngine.getLoadWorker();
  }
}
