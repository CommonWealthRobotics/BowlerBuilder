package com.neuronrobotics.bowlerbuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class BowlerBuilderTest extends FxRobot {

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
  void preferencesContinueToViewTest() {
    clickOn("3D CAD").clickOn("New File");

    assertTrue(lookup("#webView").tryQuery().isPresent());
  }

}
