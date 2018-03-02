package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.controller.module.CADModelViewerControllerModule;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import java.io.IOException;
import java.util.Arrays;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class CADModelViewerTest extends AbstractAutoClosingApplicationTest {

  private CADModelViewerController controller;

  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader loader = new FXMLLoader(
        getClass().getResource("../view/CADModelViewer.fxml"),
        null,
        null,
        BowlerBuilder.getInjector()
            .createChildInjector(new CADModelViewerControllerModule())::getInstance);
    final BorderPane mainWindow = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  @AfterEach
  void afterEach() {
    FxHelper.runAndWait(() -> controller.clearMeshes());
  }

  @Test
  void addCSGTest() {
    final CSG foo = new Cube(1, 1, 1).toCSG();
    controller.addCSG(foo);

    assertTrue(controller.getCsgMap().containsKey(foo));
  }

  @Test
  void addCSGsTest() {
    final CSG foo = new Cube(1, 1, 1).toCSG();
    final CSG bar = new Cube(2, 1, 1).toCSG();
    controller.addAllCSGs(foo, bar);

    assertTrue(controller.getCsgMap().containsKey(foo));
    assertTrue(controller.getCsgMap().containsKey(bar));
  }

  @Test
  void addCSGs2Test() {
    final CSG foo = new Cube(1, 1, 1).toCSG();
    final CSG bar = new Cube(2, 1, 1).toCSG();
    controller.addAllCSGs(Arrays.asList(foo, bar));

    assertTrue(controller.getCsgMap().containsKey(foo));
    assertTrue(controller.getCsgMap().containsKey(bar));
  }

}
