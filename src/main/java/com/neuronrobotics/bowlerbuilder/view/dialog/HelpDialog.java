package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController;
import java.io.IOException;
import java.util.logging.Level;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class HelpDialog extends Dialog {

  public HelpDialog() {
    super();

    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/dialog/EditorHelp.fxml"));
    setTitle("BowlerBuilder Help");
    getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    try {
      getDialogPane().setContent(loader.load());
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load Editor Help Dialog.\n" + Throwables.getStackTraceAsString(e));
    }
  }

}
