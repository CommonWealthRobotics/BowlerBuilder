/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.view.tab;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.view.tab.CreatureLabTab;
import java.io.IOException;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class CreatureLabTest extends AbstractAutoClosingApplicationTest {

  @Override
  public void start(final Stage stage) throws IOException {
    final CreatureLabTab tab = new CreatureLabTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  @Test
  void testStart() {
    // Nothing here, just test to make sure we start without exceptions
  }
}
