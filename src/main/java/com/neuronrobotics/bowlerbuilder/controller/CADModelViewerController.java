package com.neuronrobotics.bowlerbuilder.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CADModelViewerController implements Initializable {

  @FXML
  private AnchorPane root;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Box
    Box testBox = new Box(5, 5, 5);
    testBox.setMaterial(new PhongMaterial(Color.RED));
    testBox.setDrawMode(DrawMode.LINE);

    // Create and position camera
    PerspectiveCamera camera = new PerspectiveCamera(true);
    camera.getTransforms().addAll (
        new Rotate(-20, Rotate.Y_AXIS),
        new Rotate(-20, Rotate.X_AXIS),
        new Translate(0, 0, -15));

    // Build the Scene Graph
    Group sceneGraph = new Group();
    sceneGraph.getChildren().add(camera);
    sceneGraph.getChildren().add(testBox);

    // Use a SubScene
    SubScene subScene = new SubScene(sceneGraph, 300,300);
    subScene.setFill(Color.ALICEBLUE);
    subScene.setCamera(camera);
    Group scenes = new Group();
    scenes.getChildren().add(subScene);
    root.getChildren().add(scenes);
  }

}
