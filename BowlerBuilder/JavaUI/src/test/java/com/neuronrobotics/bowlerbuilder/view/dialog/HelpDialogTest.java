/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class HelpDialogTest extends AbstractAutoClosingApplicationTest {

  public HelpDialogTest() {
    super();
  }

  @Override
  public void start(final Stage stage) {
    final HelpDialog dialog = new HelpDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void showTest() {
    assertTrue(lookup("#editorGuide").tryQuery().isPresent());
  }
}
