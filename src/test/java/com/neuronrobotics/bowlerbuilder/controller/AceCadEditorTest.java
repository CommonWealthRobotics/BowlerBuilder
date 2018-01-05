package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.Guice;
import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.controller.module.AceCadEditorControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.module.CadModelViewerControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditorView;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AceCadEditorTest extends AutoClosingApplicationTest {

  private AceCadEditorController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("../view/AceCadEditor.fxml"),
        null,
        null,
        Guice.createInjector(
            new AceCadEditorControllerModule(
                BowlerBuilder.getInjector().getInstance(AceEditorView.class)),
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
  void basicRunButtonTest() {
    FxHelper.runAndWait(() -> controller.insertAtCursor("CSG foo=new Cube(10,10,10).toCSG()"));
    FxHelper.runAndWait(() -> ((Button) lookup("#runButton").query()).fire());

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

  @Test
  void runCubeTest() {
    FxHelper.runAndWait(() -> controller.insertAtCursor("CSG foo=new Cube(10,10,10).toCSG()"));
    controller.runEditorContent();

    assertEquals(1, controller.getCADViewerController().getCsgMap().size());
  }

  @Test
  void runStringScriptTest() {
    assertTrue(
        controller.runStringScript("CSG foo = new Sphere(10).toCSG();",
            new ArrayList<>(),
            "Groovy")
            instanceof CSG);
  }

  @Test
  void getTextTest() throws ExecutionException, InterruptedException {
    FxHelper.runAndWait(() -> controller.getScriptEditor().insertAtCursor("foo\nbar"));
    assertEquals("foo\nbar", FxUtil.returnFX(() -> controller.getScriptEditor().getText()));
  }

}
