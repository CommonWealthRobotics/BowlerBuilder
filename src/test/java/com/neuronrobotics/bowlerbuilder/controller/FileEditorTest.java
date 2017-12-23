package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.Guice;
import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.CadModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace.AceEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner.ScriptRunner;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class FileEditorTest extends AutoClosingApplicationTest {

  private AceCadEditorController controller;
  private ScriptRunner scriptRunner;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("../view/AceCadEditor.fxml"),
        null,
        null,
        Guice.createInjector(
            new AceCadEditorControllerModule(new AceEditorView()),
            new CadModelViewerControllerModule())::getInstance);
    SplitPane mainWindow = loader.load();
    controller = loader.getController();
    scriptRunner = controller.getScriptRunner();
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
    BooleanProperty doneCompiling = new SimpleBooleanProperty(false);
    BooleanProperty doneRunning = new SimpleBooleanProperty(false);

    controller.getScriptRunner().scriptCompilingProperty().addListener(observable ->
        doneCompiling.setValue(true));
    controller.getScriptRunner().scriptRunningProperty().addListener(observable ->
        doneRunning.setValue(true));

    FxHelper.runAndWait(() -> controller.insertAtCursor("CSG foo=new Cube(10,10,10).toCSG()"));
    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());

    while (!doneCompiling.getValue() || !doneRunning.getValue()) {
      ThreadUtil.wait(100);
    }

    assertEquals(1, controller.getCADViewerController().getCsgMap().size());
  }

}
