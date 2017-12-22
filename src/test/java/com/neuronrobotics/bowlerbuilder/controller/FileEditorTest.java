package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.Guice;
import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.controller.module.CadModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.FileEditorControllerModule;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class FileEditorTest extends AutoClosingApplicationTest {

  private AceCadEditorController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("../view/AceCadEditor.fxml"),
        null,
        null,
        Guice.createInjector(
            new FileEditorControllerModule(scriptEditorView),
            new CadModelViewerControllerModule())::getInstance);
    SplitPane mainWindow = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  @Test
  void runEmptyFileTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

  @Test
  void runCubeTest() {
    FxHelper.runAndWait(() -> controller.insertAtCursor("CSG foo=new Cube(10,10,10).toCSG()"));
    ThreadUtil.wait(2000);

    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());
    ThreadUtil.wait(2000);

    assertEquals(1, controller.getCADViewerController().getCsgMap().size());
  }

}
