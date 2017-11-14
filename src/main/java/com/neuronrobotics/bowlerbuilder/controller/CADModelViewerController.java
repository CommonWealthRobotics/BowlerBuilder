package com.neuronrobotics.bowlerbuilder.controller;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CADModelViewerController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private Button homeCameraButton;

  //Viewing camera and its transforms
  private final PerspectiveCamera camera; //NOPMD
  private final Rotate rotateX;
  private final Rotate rotateY;
  private final Rotate rotateZ;
  private final Translate translate;
  private double mousePosX; //NOPMD
  private double mousePosY; //NOPMD
  private static final double mouseXSens = 10;
  private static final double mouseYSens = 10;

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
    subScene.setManaged(false);
    subScene.setFill(Color.ALICEBLUE);
    subScene.setCamera(camera);
    subScene.setId("cadViewerSubScene");

    subScene.setOnMousePressed((MouseEvent me) -> {
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
    });

    subScene.setOnMouseDragged((MouseEvent me) -> {
      double dx = mousePosX - me.getSceneX();
      double dy = mousePosY - me.getSceneY();
      if (me.isPrimaryButtonDown()) {
        rotateX.setAngle(rotateX.getAngle() + (dy / mouseYSens * 360) * (Math.PI / 180));
        rotateY.setAngle(rotateY.getAngle() + (dx / mouseXSens * -360) * (Math.PI / 180));
      }
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
    });
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //Resize the subscene with the borderpane
    subScene.heightProperty().bind(root.heightProperty());
    subScene.widthProperty().bind(root.widthProperty());

    //Clip the subscene so it doesn't overlap with other borderpane elements
    final Rectangle clip = new Rectangle();
    subScene.setClip(clip);
    subScene.layoutBoundsProperty().addListener((observableValue, oldBounds, newBounds) -> {
      clip.setWidth(newBounds.getWidth());
      clip.setHeight(newBounds.getHeight() - 35); //35 is the height of the bottom HBox
    });

    root.setCenter(subScene);

    addCSG(new Cube(1, 1, 1).toCSG());
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

  @FXML
  private void onHomeCamera(ActionEvent actionEvent) {
    homeCamera();
  }

  /**
   * Homes the camera rotation and translation.
   */
  public void homeCamera() {
    rotateX.setAngle(0);
    rotateY.setAngle(0);
    rotateZ.setAngle(0);
    translate.setX(0);
    translate.setY(0);
    translate.setZ(-15);
  }

  public Rotate getCameraRotateX() {
    return rotateX;
  }

  public Rotate getCameraRotateY() {
    return rotateY;
  }

  public Rotate getCameraRotateZ() {
    return rotateZ;
  }

  public Translate getCameraTranslate() {
    return translate;
  }
}