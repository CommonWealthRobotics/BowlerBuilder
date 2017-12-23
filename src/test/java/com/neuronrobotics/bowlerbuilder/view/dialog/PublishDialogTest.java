package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class PublishDialogTest extends AutoClosingApplicationTest {

  private PublishDialog dialog;

  @Override
  public void start(Stage stage) {
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
