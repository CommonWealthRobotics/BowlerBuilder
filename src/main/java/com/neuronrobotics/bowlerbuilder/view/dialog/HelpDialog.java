package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class HelpDialog extends Dialog {

  private static final Logger logger = Logger.getLogger(HelpDialog.class.getSimpleName());

  public HelpDialog() {
    super();

    LoggerUtilities.setupLogger(logger);

    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/dialog/EditorHelp.fxml"));

    setTitle("BowlerBuilder Help");
    getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    try {
      getDialogPane().setContent(loader.load());
    } catch (IOException e) {
      logger.log(Level.SEVERE,
          "Could not load Editor Help Dialog.\n" + Throwables.getStackTraceAsString(e));
    }
  }

}
