/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class PublishDialogTest extends AbstractAutoClosingApplicationTest {

  private PublishDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new PublishDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void commitMsgTest() {
    clickOn("#commitMessageTextArea").write("a").clickOn("OK");

    assertEquals("a", dialog.getResult());
    assertEquals("a", dialog.getCommitMessage());
  }

  @Test
  void cancelTest() {
    clickOn("#commitMessageTextArea").write("a").clickOn("Cancel");

    assertNull(dialog.getResult());
  }
}
