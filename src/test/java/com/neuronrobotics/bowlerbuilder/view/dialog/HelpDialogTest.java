package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class HelpDialogTest extends AutoClosingApplicationTest {

  private HelpDialog dialog;

  @Override
  public void start(Stage stage) throws Exception {
    dialog = new HelpDialog();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void showTest() {
    assertTrue(lookup("#editorGuide").tryQuery().isPresent());
  }

}
