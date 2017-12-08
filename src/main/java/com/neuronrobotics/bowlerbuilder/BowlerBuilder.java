package com.neuronrobotics.bowlerbuilder;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BowlerBuilder extends Application {

  @Override
  public void start(Stage primaryStage) throws IOException {
    System.out.println("Start");
    FXMLLoader loader = new FXMLLoader(BowlerBuilder.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"));
    System.out.println("Make FXML loader");
    Pane mainWindow = loader.load();
    System.out.println("Main window loaded");

    primaryStage.setTitle("BowlerBuilder");
    primaryStage.setScene(new Scene(mainWindow));
    System.out.println("Scene set");
    primaryStage.show();
    System.out.println("Stage showing");
  }

}
