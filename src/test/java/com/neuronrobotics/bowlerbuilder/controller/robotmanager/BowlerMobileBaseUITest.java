package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import java.util.ArrayList;
import java.util.Collections;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

public class BowlerMobileBaseUITest extends AbstractAutoClosingApplicationTest {

  private BowlerMobileBaseUI controller;

  @Override
  public void start(Stage stage) {
    controller = BowlerBuilder.getInjector()
        .createChildInjector(new CADModelViewerControllerModule())
        .getInstance(BowlerMobileBaseUI.class);
  }

  @AfterEach
  void afterEach() {
    FxHelper.runAndWait(() -> controller.setAllCSG(new ArrayList<>(), null));
  }

  @Test
  void addCSGTest() {
    final CSG foo = new Cube(1, 1, 1).toCSG();
    controller.addCSG(Collections.singleton(foo), null);
    WaitForAsyncUtils.waitForFxEvents();

    assertTrue(controller.getVisibleCSGs().contains(foo));
  }

  @Test
  void addCSGsTest() {
    final CSG foo = new Cube(1, 1, 1).toCSG();
    final CSG bar = new Cube(2, 1, 1).toCSG();
    controller.addCSG(ImmutableList.of(foo, bar), null);
    WaitForAsyncUtils.waitForFxEvents();

    assertTrue(controller.getVisibleCSGs().contains(foo));
    assertTrue(controller.getVisibleCSGs().contains(bar));
  }

}
