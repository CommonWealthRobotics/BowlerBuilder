package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

class BowlerBuilderApplicationTest extends FxRobot {

  @BeforeEach
  void before() throws Exception {
    FxToolkit.registerPrimaryStage();
    Thread fxThread = new Thread(() -> {
      try {
        FxToolkit.setupApplication(BowlerBuilder::new);
      } catch (TimeoutException e) {
        fail();
      }
    });
    fxThread.start();
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void testStart() {
    //Nothing here, just test to make sure we start without exceptions
  }

}
