package com.neuronrobotics.bowlerbuilder;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BowlerBuilder extends Application {

  private static final ButtonType START = new ButtonType("Start", ButtonData.OK_DONE);
  private static final ButtonType QUIT = new ButtonType("Quit", ButtonData.CANCEL_CLOSE);

  @Override
  public void start(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader(BowlerBuilder.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"));
    Pane mainWindow = loader.load();

    primaryStage.setTitle("BowlerBuilder");
    primaryStage.setScene(new Scene(mainWindow));
    primaryStage.show();
  }

}
