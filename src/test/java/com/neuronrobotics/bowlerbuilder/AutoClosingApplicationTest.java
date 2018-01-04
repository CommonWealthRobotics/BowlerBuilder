package com.neuronrobotics.bowlerbuilder;

import org.junit.jupiter.api.AfterEach;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * A helpful version of {@link ApplicationTest} that automatically cleans up windows after it
 * finishes.
 */
public abstract class AutoClosingApplicationTest extends ApplicationTest {

  @AfterEach
  public void closeAllStages() {
    listWindows().forEach(window -> FxHelper.runAndWait(window::hide));
    WaitForAsyncUtils.waitForFxEvents();
  }

}
