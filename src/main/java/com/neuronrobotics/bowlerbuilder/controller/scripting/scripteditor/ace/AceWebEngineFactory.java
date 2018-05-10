/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import javafx.scene.web.WebEngine;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AceWebEngineFactory {

  public AceWebEngineFactory() {}

  public AceWebEngine create(final WebEngine webEngine) {
    return new AceWebEngine(webEngine);
  }
}
