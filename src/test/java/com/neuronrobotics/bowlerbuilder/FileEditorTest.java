package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

class FileEditorTest extends FxRobot {

  @BeforeEach
  void before() throws Exception {
    FxToolkit.registerPrimaryStage();
    Thread fxThread = new Thread(() -> {
      try {
        FxToolkit.setupApplication(BowlerBuilder::new);
      } catch (TimeoutException ex) {
        fail();
      }
    });
    fxThread.start();
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void cadViewerTest() {
    clickOn("3D CAD").clickOn("New File").clickOn("Run");

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

  @Test
  void cadViewerHomeCameraTest() {
    clickOn("3D CAD").clickOn("New File").clickOn("Run");
    clickOn("#cadViewerSubScene")
        .drag(MouseButton.PRIMARY)
        .dropTo(lookup("#cadViewerSubScene").query().localToScreen(20, 20));
    clickOn("Home Camera");

    assertTrue(lookup("#cadViewerBorderPane").tryQuery().isPresent());
  }

}
