package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class GistFileSelectionDialogTest extends AutoClosingApplicationTest {

  private GistFileSelectionDialog dialog;

  @Override
  public void start(Stage stage) {
    dialog = new GistFileSelectionDialog("", text -> true);
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void basicTest() {
    //TODO: Maybe have to mock ScriptingEngine.filesInGit
  }

}
