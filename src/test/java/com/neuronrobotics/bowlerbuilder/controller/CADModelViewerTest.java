package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerStudio3dEngine;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CADModelViewerTest {
  private final CADModelViewerController controller
      = new CADModelViewerController(new BowlerStudio3dEngine());

  @AfterEach
  void afterEach() {
    controller.clearMeshes();
  }

  @Test
  void addCSGTest() {
    CSG foo = new Cube(1, 1, 1).toCSG();
    controller.addCSG(foo);

    assertTrue(controller.getCsgMap().containsKey(foo));
  }

  @Test
  void addCSGsTest() {
    CSG foo = new Cube(1, 1, 1).toCSG();
    CSG bar = new Cube(2, 1, 1).toCSG();
    controller.addAllCSGs(foo, bar);

    assertTrue(controller.getCsgMap().containsKey(foo));
    assertTrue(controller.getCsgMap().containsKey(bar));
  }

  @Test
  void addCSGs2Test() {
    CSG foo = new Cube(1, 1, 1).toCSG();
    CSG bar = new Cube(2, 1, 1).toCSG();
    controller.addAllCSGs(Arrays.asList(foo, bar));

    assertTrue(controller.getCsgMap().containsKey(foo));
    assertTrue(controller.getCsgMap().containsKey(bar));
  }

}
