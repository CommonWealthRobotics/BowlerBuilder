package com.neuronrobotics.bowlerbuilder.controller;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class CADModelViewerController implements Initializable {

  @FXML
  private AnchorPane root;

  //Viewing camera and its transforms
  private final PerspectiveCamera camera;
  private final Rotate rotateX;
  private final Rotate rotateY;
  private final Rotate rotateZ;
  private final Translate translate;

  public CADModelViewerController() {
    rotateX = new Rotate(0, Rotate.X_AXIS);
    rotateY = new Rotate(0, Rotate.Y_AXIS);
    rotateZ = new Rotate(0, Rotate.Z_AXIS);
    translate = new Translate(0, 0, -15);
    camera = new PerspectiveCamera(true);
    camera.getTransforms().addAll(
        rotateX,
        rotateY,
        rotateZ,
        translate);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CSG cube = new Cube(1, 1, 1).toCSG();

    Consumer<Rotate> makeTimelines = rotate -> {
      Timeline timeline = new Timeline(
          new KeyFrame(Duration.seconds(0), new KeyValue(rotate.angleProperty(), 0)),
          new KeyFrame(Duration.seconds(15), new KeyValue(rotate.angleProperty(), 360)));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.play();
    };
    makeTimelines.accept(rotateX);
    makeTimelines.accept(rotateY);
    makeTimelines.accept(rotateZ);

    // Build the Scene Graph
    Group sceneGraph = new Group();
    sceneGraph.getChildren().add(camera);
    cube.toJavaFXMesh().getAsMeshViews().forEach(mesh -> {
      mesh.setMaterial(new PhongMaterial(Color.RED));
      mesh.setDrawMode(DrawMode.FILL);
      mesh.setDepthTest(DepthTest.ENABLE);
      mesh.setCullFace(CullFace.BACK);
      sceneGraph.getChildren().add(mesh);
    });

    // Use a SubScene
    SubScene subScene = new SubScene(sceneGraph, 300, 300);
    subScene.setFill(Color.ALICEBLUE);
    subScene.setCamera(camera);
    Group scenes = new Group();
    scenes.getChildren().add(subScene);
    root.getChildren().add(scenes);
  }

}
