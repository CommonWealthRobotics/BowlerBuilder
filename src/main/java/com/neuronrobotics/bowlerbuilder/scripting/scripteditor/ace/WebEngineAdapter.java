package com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ace;

import javafx.concurrent.Worker;

/**
 * Adapter to a {@link javafx.scene.web.WebEngine} since that class is final.
 */
public interface WebEngineAdapter {

  /**
   * Execute a script and return the result.
   *
   * @param script script code
   * @return result
   */
  Object executeScript(String script);

  /**
   * Get the load worker.
   *
   * @return load worker
   */
  Worker<Void> getLoadWorker();

}
