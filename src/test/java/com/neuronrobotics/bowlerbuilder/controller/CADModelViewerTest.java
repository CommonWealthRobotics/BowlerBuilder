package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertTrue;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class CADModelViewerTest {
  private final CADModelViewerController controller = new CADModelViewerController();

  @Test
  void addCSGTest() {
    CSG foo = new Cube(1,1,1).toCSG();
    controller.addCSG(foo);

    assertTrue(controller.getEngine().getCsgMap().containsKey(foo));
  }

  @Test
  void addCSGsTest() {
    CSG foo = new Cube(1,1,1).toCSG();
    CSG bar = new Cube(2,1,1).toCSG();
    controller.addAllCSGs(foo, bar);

    assertTrue(controller.getEngine().getCsgMap().containsKey(foo));
    assertTrue(controller.getEngine().getCsgMap().containsKey(bar));
  }

  @Test
  void addCSGs2Test() {
    CSG foo = new Cube(1,1,1).toCSG();
    CSG bar = new Cube(2,1,1).toCSG();
    controller.addAllCSGs(Arrays.asList(foo, bar));

    assertTrue(controller.getEngine().getCsgMap().containsKey(foo));
    assertTrue(controller.getEngine().getCsgMap().containsKey(bar));
  }
}
