package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class GistFileSelectionDialogTest extends AbstractAutoClosingApplicationTest {

  @Override
  public void start(final Stage stage) {
    final GistFileSelectionDialog dialog = new GistFileSelectionDialog("", text -> true);
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void basicTest() {
    //TODO: Maybe have to mock ScriptingEngine.filesInGit
  }

}
