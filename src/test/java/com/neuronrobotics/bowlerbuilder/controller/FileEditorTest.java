package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class FileEditorTest extends AutoClosingApplicationTest {

  private FileEditorController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/FileEditor.fxml"));
    SplitPane mainWindow = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  //  @AfterEach
  //  void afterEach() {
  //    closeCurrentWindow();
  //  }

  @Test
  void runEmptyFileTest() {
    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

  @Test
  void runCubeTest() {
    clickOn("#webView").write("CSG foo=new Cube(10,10,10).toCSG()");
    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());

    ThreadUtil.wait(2000);

    assertEquals(1, controller.getCADViewerController().getEngine().getCsgMap().size());
  }

}
