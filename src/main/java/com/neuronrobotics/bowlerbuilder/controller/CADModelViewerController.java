package com.neuronrobotics.bowlerbuilder.controller;

import eu.mihosoft.jcsg.CSG;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class CADModelViewerController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private Button homeCameraButton;

  //Real camera
  private final XFormCamera cameraXForm;
  private final Translate translate;
  private double mousePosX; //NOPMD
  private double mousePosY; //NOPMD
  private static final double mouseXSens = 15;
  private static final double mouseYSens = 15;
  private static final double zoomSens = 0.2;
  private static final double zoomFineSens = 0.05;

  //Main scene graph for all CAD objects
  private final Group sceneGraph;

  //Subscene to show all scene graphs
  private final SubScene subScene;

  public CADModelViewerController() {
    translate = new Translate(0, 0, -15);
    PerspectiveCamera camera = new PerspectiveCamera(true);
    cameraXForm = new XFormCamera();
    cameraXForm.getChildren().add(camera);
    camera.getTransforms().addAll(translate);

    sceneGraph = new Group();
    sceneGraph.getChildren().add(cameraXForm);
    subScene = new SubScene(sceneGraph, 300, 300);
    subScene.setManaged(false);
    subScene.setFill(Color.ALICEBLUE);
    subScene.setCamera(camera);
    subScene.setId("cadViewerSubScene");

    //Keep track of drag start location
    subScene.setOnMousePressed((MouseEvent me) -> {
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
    });

    //Keep track of drag movement and update rotation
    subScene.setOnMouseDragged((MouseEvent me) -> {
      double dx = mousePosX - me.getSceneX();
      double dy = mousePosY - me.getSceneY();
      if (me.isPrimaryButtonDown()) {
        //Primary button is rotate
        cameraXForm.rotateY((dx / mouseXSens * -360) * (Math.PI / 180));
        cameraXForm.rotateX((dy / mouseYSens * 360) * (Math.PI / 180));
      } else if (me.isMiddleButtonDown()) {
        //Middle button is fine zoom
        translateCamera(0, 0, dy * zoomFineSens);
      }
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
    });

    subScene.setOnScroll(event -> {
      translateCamera(0, 0, event.getDeltaY() * zoomSens);
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
  }

  /**
   * Add a MeshView to the scene graph.
   *
   * @param mesh MeshView to add
   */
  public void addMeshView(MeshView mesh) {
    mesh.setMaterial(new PhongMaterial(Color.RED));
    mesh.setDrawMode(DrawMode.FILL);
    mesh.setDepthTest(DepthTest.ENABLE);
    mesh.setCullFace(CullFace.BACK);

    mesh.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);

        //Wireframe/Solid draw toggle
        MenuItem wireframe;

        //Set the title of the MenuItem to the opposite of the current draw
        if (mesh.getDrawMode().equals(DrawMode.LINE)) {
          wireframe = new MenuItem("Show As Solid");
        } else {
          wireframe = new MenuItem("Show As Wireframe");
        }

        //Set the onAction of the MenuItem to flip the draw state
        wireframe.setOnAction(actionEvent -> {
          if (mesh.getDrawMode().equals(DrawMode.FILL)) {
            mesh.setDrawMode(DrawMode.LINE);
            wireframe.setText("Show As Solid");
          } else {
            mesh.setDrawMode(DrawMode.FILL);
            wireframe.setText("Show As Wireframe");
          }
        });

        menu.getItems().addAll(wireframe);
        //Need to set the root as mesh.getScene().getWindow() so setAutoHide() works when we
        //right-click somewhere else
        mesh.setOnContextMenuRequested(event ->
            menu.show(mesh.getScene().getWindow(), event.getScreenX(), event.getScreenY()));
      }
    });

    sceneGraph.getChildren().add(mesh);
  }

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  public void addMeshesFromCSG(CSG csg) {
    csg.toJavaFXMesh().getAsMeshViews().forEach(this::addMeshView);
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs CSGs to add
   */
  public void addMeshesFromAllCSG(CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addMeshesFromCSG);
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs List of CSGs to add
   */
  public void addMeshesFromAllCSG(List<CSG> csgs) {
    csgs.forEach(this::addMeshesFromCSG);
  }

  /**
   * Rotate the camera. Adds to the existing rotation.
   *
   * @param rotX X axis rotation
   * @param rotY Y axis rotation
   * @param rotZ Z axis rotation
   */
  public void rotateCamera(double rotX, double rotY, double rotZ) {
    cameraXForm.rotateX(rotX);
    cameraXForm.rotateY(rotY);
    cameraXForm.rotateZ(rotZ);
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
    cameraXForm.home();
    translate.setX(0);
    translate.setY(0);
    translate.setZ(-15);
  }

  public double getCameraRotateX() {
    return cameraXForm.getRotX();
  }

  public double getCameraRotateY() {
    return cameraXForm.getRotY();
  }

  public double getCameraRotateZ() {
    return cameraXForm.getRotZ();
  }

  public Translate getCameraTranslate() {
    return translate;
  }

  /**
   * Apply rotations iteratively to a group so the camera stays locked to azimuth rotations.
   */
  private static class XFormCamera extends Group {
    private Rotate rotation;
    private double rotX;
    private double rotY;
    private double rotZ;
    private Transform transform = new Rotate();

    XFormCamera() {
      super();
      rotX = 0;
      rotY = 0;
      rotZ = 0;
    }

    void rotateX(double angle) {
      rotation = new Rotate(angle, Rotate.X_AXIS);
      rotX += angle;
      transform = transform.createConcatenation(rotation);
      this.getTransforms().clear();
      this.getTransforms().addAll(transform);
    }

    void rotateY(double angle) {
      rotation = new Rotate(angle, Rotate.Y_AXIS);
      rotY += angle;
      transform = transform.createConcatenation(rotation);
      this.getTransforms().clear();
      this.getTransforms().addAll(transform);
    }

    void rotateZ(double angle) {
      rotation = new Rotate(angle, Rotate.Z_AXIS);
      rotZ += angle;
      transform = transform.createConcatenation(rotation);
      this.getTransforms().clear();
      this.getTransforms().addAll(transform);
    }

    void home() {
      rotX = 0;
      rotY = 0;
      rotZ = 0;
      transform = new Rotate(0, Rotate.X_AXIS);
      transform = transform.createConcatenation(new Rotate(0, Rotate.Y_AXIS));
      transform = transform.createConcatenation(new Rotate(0, Rotate.Z_AXIS));
      this.getTransforms().clear();
      this.getTransforms().addAll(transform);
    }

    public double getRotX() {
      return rotX;
    }

    public double getRotY() {
      return rotY;
    }

    public double getRotZ() {
      return rotZ;
    }
  }

}