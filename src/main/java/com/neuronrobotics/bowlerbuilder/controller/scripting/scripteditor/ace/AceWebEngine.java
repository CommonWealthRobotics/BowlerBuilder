package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javax.annotation.Nonnull;

/**
 * Simple passthrough to the real {@link WebEngine}.
 */
public class AceWebEngine implements WebEngineAdapter {

  private final WebEngine webEngine;

  public AceWebEngine(@Nonnull final WebEngine webEngine) {
    this.webEngine = webEngine;
  }

  @Override
  public Object executeScript(@Nonnull final String script) {
    return webEngine.executeScript(script);
  }

  @Override
  public Worker<Void> getLoadWorker() {
    return webEngine.getLoadWorker();
  }
}
