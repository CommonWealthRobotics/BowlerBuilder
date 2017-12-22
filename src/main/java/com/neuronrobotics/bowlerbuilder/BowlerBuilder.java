package com.neuronrobotics.bowlerbuilder;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BowlerBuilder extends Application {

  private static final Logger logger = Logger.getLogger(BowlerBuilder.class.getSimpleName());

  public BowlerBuilder() {
    LoggerUtilities.setupLogger(logger);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    //Log uncaught exceptions on the FX thread
    Thread.currentThread().setUncaughtExceptionHandler((t, e) ->
        logger.log(Level.SEVERE, Throwables.getStackTraceAsString(e)));

    Injector injector = Guice.createInjector();
    FXMLLoader loader = new FXMLLoader(
        BowlerBuilder.class.getResource("/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"),
        null,
        null,
        injector::getInstance);
    Pane mainWindow = loader.load();

    primaryStage.setTitle("BowlerBuilder");
    primaryStage.setScene(new Scene(mainWindow));
    primaryStage.setOnCloseRequest(event ->
        ((MainWindowController) loader.getController()).saveAndQuit());
    primaryStage.show();
  }

}
