package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

class BowlerBuilderTest extends AutoClosingApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
    Pane mainWindow = loader.load();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  @Test
  void newFileTest() {
    clickOn("3D CAD").clickOn("New File");

    assertTrue(lookup("#newFileDialog").tryQuery().isPresent());
  }

  @Test
  void closeTest() {
    clickOn("File").clickOn("Exit");

    assertFalse(lookup("#root").tryQuery().isPresent());
  }

}
