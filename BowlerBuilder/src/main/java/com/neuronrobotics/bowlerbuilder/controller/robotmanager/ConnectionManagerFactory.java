/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import javafx.scene.control.Accordion;
import javafx.scene.layout.HBox;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConnectionManagerFactory {

  public ConnectionManagerFactory() {}

  public ConnectionManager get(final HBox connectionsHeader, final Accordion accordion) {
    return new ConnectionManager(connectionsHeader, accordion);
  }
}
