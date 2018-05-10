/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javax.annotation.ParametersAreNonnullByDefault;

/** Simple passthrough to the real {@link WebEngine}. */
@ParametersAreNonnullByDefault
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
