/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import javafx.scene.control.Accordion;
import javafx.scene.layout.HBox;
import javax.annotation.Nonnull;

public class ConnectionManagerFactory {

  public ConnectionManager get(@Nonnull final HBox connectionsHeader,
      @Nonnull final Accordion accordion) {
    return new ConnectionManager(connectionsHeader, accordion);
  }

}
