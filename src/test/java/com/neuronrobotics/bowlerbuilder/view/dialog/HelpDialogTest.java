/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class HelpDialogTest extends AutoClosingApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    HelpDialog dialog = new HelpDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void showTest() {
    assertTrue(lookup("#editorGuide").tryQuery().isPresent());
  }

}
