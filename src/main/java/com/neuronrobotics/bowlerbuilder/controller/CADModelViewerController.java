package com.neuronrobotics.bowlerbuilder.controller;

import eu.mihosoft.jcsg.CSG;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
  private final PerspectiveCamera camera; //NOPMD
  private final Rotate rotateX;
  private final Rotate rotateY;
  private final Rotate rotateZ;
  private final Translate translate;

  //Main scene graph for all CAD objects
  private final Group sceneGraph;

  //Subscene to show all scene graphs
  private final SubScene subScene;

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

    sceneGraph = new Group();
    sceneGraph.getChildren().add(camera);

    subScene = new SubScene(sceneGraph, 300, 300);
    subScene.setFill(Color.ALICEBLUE);
    subScene.setCamera(camera);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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

    root.getChildren().add(subScene);
  }

  /**
   * Add a CSG to the scene graph. All meshes in the CSG will be added
   *
   * @param csg CSG to add
   */
  public void addCSG(CSG csg) {
    csg.toJavaFXMesh().getAsMeshViews().forEach(mesh -> {
      mesh.setMaterial(new PhongMaterial(Color.RED));
      mesh.setDrawMode(DrawMode.FILL);
      mesh.setDepthTest(DepthTest.ENABLE);
      mesh.setCullFace(CullFace.BACK);
      sceneGraph.getChildren().add(mesh);
    });
  }

  /**
   * Add all given CSGs to the scene graph.
   *
   * @param csgs CSGs to add
   */
  public void addAllCSG(CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addCSG);
  }

  /**
   * Add all CSGs to the scene graph.
   *
   * @param csgs List of CSGs to add
   */
  public void addAllCSG(List<CSG> csgs) {
    csgs.forEach(this::addCSG);
  }

  /**
   * Rotate the camera. Adds to the existing rotation.
   *
   * @param rotX X axis rotation
   * @param rotY Y axis rotation
   * @param rotZ Z axis rotation
   */
  public void rotateCamera(double rotX, double rotY, double rotZ) {
    rotateX.setAngle(rotateX.getAngle() + rotX);
    rotateY.setAngle(rotateY.getAngle() + rotY);
    rotateZ.setAngle(rotateZ.getAngle() + rotZ);
  }

  /**
   * Translate the camera. Adds to the existing translation.
   *
   * @param movX X axis translation
   * @param movY Y axis translation
   * @param movZ Z axis translation
   */
  public void translateCamera(double movX, double movY, double movZ) {
    translate.setX(translate.getX() + movX);
    translate.setY(translate.getY() + movY);
    translate.setZ(translate.getZ() + movZ);
  }

}