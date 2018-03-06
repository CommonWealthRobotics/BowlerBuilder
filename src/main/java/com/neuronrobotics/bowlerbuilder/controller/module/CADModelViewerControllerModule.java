/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerCadEngine;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;

public class CADModelViewerControllerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CadEngine.class).to(BowlerCadEngine.class);
  }

}
