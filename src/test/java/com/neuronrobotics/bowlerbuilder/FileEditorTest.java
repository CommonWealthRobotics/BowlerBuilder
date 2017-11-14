package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.controller.FileEditorController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class FileEditorTest extends ApplicationTest {

  private FileEditorController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/FileEditor.fxml"));
    SplitPane mainWindow = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  @AfterEach
  void afterEach() {
    closeCurrentWindow();
  }

  @Test
  void runFileTest() {
    clickOn("#runButton");

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

  @Test
  void newCubeTest() {
    clickOn("#newCubeButton");

    assertTrue(lookup("#newCubeRoot").tryQuery().isPresent());
  }

  @Test
  void newRoundedCubeTest() {
    clickOn("#newRoundedCubeButton");

    assertTrue(lookup("#newRoundedCubeRoot").tryQuery().isPresent());
  }

  @Test
  void newCylinderTest() {
    clickOn("#newCylinderButton");

    assertTrue(lookup("#newCylinderRoot").tryQuery().isPresent());
  }

  @Test
  void newSphereTest() {
    clickOn("#newSphereButton");

    assertTrue(lookup("#newSphereRoot").tryQuery().isPresent());
  }

}
