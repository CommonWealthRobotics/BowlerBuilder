package com.neuronrobotics.bowlerbuilder;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController;
import java.io.IOException;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BowlerBuilder extends Application {

  @Override
  public void start(Stage primaryStage) throws IOException {
    //Log uncaught exceptions on the FX thread
    Thread.currentThread().setUncaughtExceptionHandler((t, e) ->
        LoggerUtilities.getLogger().log(Level.INFO, Throwables.getStackTraceAsString(e)));

    FXMLLoader loader = new FXMLLoader(BowlerBuilder.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"));
    Pane mainWindow = loader.load();

    primaryStage.setTitle("BowlerBuilder");
    primaryStage.setScene(new Scene(mainWindow));
    primaryStage.setOnCloseRequest(event -> ((MainWindowController) loader.getController()).saveAndQuit());
    primaryStage.show();
  }

}
