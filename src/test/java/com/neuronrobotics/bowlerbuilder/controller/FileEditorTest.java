package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class FileEditorTest extends AutoClosingApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/FileEditor.fxml"));
    SplitPane mainWindow = loader.load();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  @AfterEach
  void afterEach() {
    closeCurrentWindow();
  }

  @Test
  void runFileTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

  @Test
  void newCubeTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#newCubeButton").query()).fire());

    assertTrue(lookup("#newCubeRoot").tryQuery().isPresent());
  }

  @Test
  void newRoundedCubeTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#newRoundedCubeButton").query()).fire());

    assertTrue(lookup("#newRoundedCubeRoot").tryQuery().isPresent());
  }

  @Test
  void newCylinderTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#newCylinderButton").query()).fire());

    assertTrue(lookup("#newCylinderRoot").tryQuery().isPresent());
  }

  @Test
  void newSphereTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#newSphereButton").query()).fire());

    assertTrue(lookup("#newSphereRoot").tryQuery().isPresent());
  }

}
