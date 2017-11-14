package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.assertEquals;

import com.neuronrobotics.bowlerbuilder.controller.CADModelViewerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

class CADModelViewerTest extends AutoClosingApplicationTest {

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

    assertEquals(26.39, controller.getCameraRotateX(), 1);
    assertEquals(-33.93, controller.getCameraRotateY(), 1);
    assertEquals(0, controller.getCameraRotateZ(), 0);
    assertEquals(0, controller.getCameraTranslate().getX(), 0);
    assertEquals(0, controller.getCameraTranslate().getY(), 0);
    assertEquals(-15, controller.getCameraTranslate().getZ(), 0);
  }

  @Test
  void homeCameraTest() {
    clickOn("#cadViewerSubScene")
        .drag(MouseButton.PRIMARY)
        .dropTo(lookup("#cadViewerSubScene").query().localToScreen(20, 20));
    clickOn("Home Camera");

    assertEquals(0, controller.getCameraRotateX(), 0);
    assertEquals(0, controller.getCameraRotateY(), 0);
    assertEquals(0, controller.getCameraRotateZ(), 0);
    assertEquals(0, controller.getCameraTranslate().getX(), 0);
    assertEquals(0, controller.getCameraTranslate().getY(), 0);
    assertEquals(-15, controller.getCameraTranslate().getZ(), 0);
  }

  @Test
  void rotateCameraTest() {
    controller.rotateCamera(1, 2, 3);

    assertEquals(1, controller.getCameraRotateX(), 0);
    assertEquals(2, controller.getCameraRotateY(), 0);
    assertEquals(3, controller.getCameraRotateZ(), 0);
  }

  @Test
  void translateCameraTest() {
    controller.translateCamera(1, 2, 3);

    assertEquals(1, controller.getCameraTranslate().getX(), 0);
    assertEquals(2, controller.getCameraTranslate().getY(), 0);
    assertEquals(-12, controller.getCameraTranslate().getZ(), 0);
  }

}
