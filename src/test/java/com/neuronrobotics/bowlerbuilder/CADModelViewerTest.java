package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.assertEquals;

import com.neuronrobotics.bowlerbuilder.controller.CADModelViewerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class CADModelViewerTest extends ApplicationTest {

  private CADModelViewerController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/CADModelViewer.fxml"));
    Pane mainWindow = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }
  
  @Test
  void dragTest() {
    clickOn("#cadViewerSubScene")
        .drag(MouseButton.PRIMARY)
        .dropTo(lookup("#cadViewerSubScene").query().localToScreen(20, 20));

    assertEquals(controller.getCameraRotateX().getAngle(), 39.58, 1);
    assertEquals(controller.getCameraRotateY().getAngle(), -50.89, 1);
    assertEquals(controller.getCameraRotateZ().getAngle(), 0, 0);
    assertEquals(controller.getCameraTranslate().getX(), 0, 0);
    assertEquals(controller.getCameraTranslate().getY(), 0, 0);
    assertEquals(controller.getCameraTranslate().getZ(), -15, 0);
  }

  @Test
  void homeCameraTest() {
    clickOn("#cadViewerSubScene")
        .drag(MouseButton.PRIMARY)
        .dropTo(lookup("#cadViewerSubScene").query().localToScreen(20, 20));
    clickOn("Home Camera");

    assertEquals(controller.getCameraRotateX().getAngle(), 0, 0);
    assertEquals(controller.getCameraRotateY().getAngle(), 0, 0);
    assertEquals(controller.getCameraRotateZ().getAngle(), 0, 0);
    assertEquals(controller.getCameraTranslate().getX(), 0, 0);
    assertEquals(controller.getCameraTranslate().getY(), 0, 0);
    assertEquals(controller.getCameraTranslate().getZ(), -15, 0);
  }

  @Test
  void rotateCameraTest() {
    controller.rotateCamera(1, 2, 3);

    assertEquals(controller.getCameraRotateX().getAngle(), 1, 0);
    assertEquals(controller.getCameraRotateY().getAngle(), 2, 0);
    assertEquals(controller.getCameraRotateZ().getAngle(), 3, 0);
  }

  @Test
  void translateCameraTest() {
    controller.translateCamera(1, 2, 3);

    assertEquals(controller.getCameraTranslate().getX(), 1, 0);
    assertEquals(controller.getCameraTranslate().getY(), 2, 0);
    assertEquals(controller.getCameraTranslate().getZ(), -12, 0);
  }

}
