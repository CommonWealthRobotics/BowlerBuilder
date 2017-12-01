package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;

import eu.mihosoft.vrl.v3d.CSG;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
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
import javafx.stage.FileChooser;

public class CADModelViewerController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private Button homeCameraButton;

  //Real camera
  private final PerspectiveCamera camera1;
  private final XFormCamera cameraXForm1;

  private final Translate translate;

  private double mousePosX; //NOPMD
  private double mousePosY; //NOPMD
  private Point3D vecIni;
  private Point3D vecPos;
  private double distance;
  private MeshView selection;

  private static final double mouseXSens = 15;
  private static final double mouseYSens = 15;
  private static final double zoomSens = 0.2;
  private static final double zoomFineSens = 0.05;

  //Main scene graph for all CAD objects
  private final Group csgGraph;
  private final SubScene csgScene;

  //Background images
  private ImageView backgroundImage;

  public CADModelViewerController() {
    translate = new Translate(0, 0, -800);

    camera1 = new PerspectiveCamera(true);
    camera1.setFarClip(100000);
    cameraXForm1 = new XFormCamera();
    cameraXForm1.getChildren().add(camera1);
    camera1.getTransforms().addAll(translate);

    try {
      backgroundImage = new ImageView(
          new Image(CADModelViewerController.class.getResource(
              "/com/neuronrobotics/bowlerbuilder/cap.png").toURI().toString()));
    } catch (URISyntaxException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not load CAD viewer background image.\n" + Throwables.getStackTraceAsString(e));
    }

    csgGraph = new Group();
    csgGraph.getChildren().addAll(cameraXForm1, backgroundImage);
    csgScene = new SubScene(csgGraph, 300, 300, true, SceneAntialiasing.BALANCED);
    csgScene.setManaged(false);
    csgScene.setFill(Color.TRANSPARENT);
    csgScene.setCamera(camera1);
    csgScene.setId("cadViewerSubScene");

    //Keep track of drag start location
    csgScene.setOnMousePressed((MouseEvent me) -> {
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      PickResult pr = me.getPickResult();
      if (pr != null) {
        if (pr.getIntersectedNode() != null && pr.getIntersectedNode() instanceof MeshView) {
          selection = (MeshView) pr.getIntersectedNode();
        } else {
          selection = null;
        }

        distance = me.getPickResult().getIntersectedDistance();
        vecIni = unProjectDirection(
            mousePosX,
            mousePosY,
            csgScene.getWidth(),
            csgScene.getHeight());
      }
    });

    //Keep track of drag movement and update rotation
    csgScene.setOnMouseDragged((MouseEvent me) -> {
      double dx = mousePosX - me.getSceneX();
      double dy = mousePosY - me.getSceneY();

      if (me.isPrimaryButtonDown()) {
        //Primary button is rotate
        cameraXForm1.rotateY((dx / mouseXSens * -360) * (Math.PI / 180));
        cameraXForm1.rotateX((dy / mouseYSens * 360) * (Math.PI / 180));
      } else if (me.isMiddleButtonDown()) {
        //Middle button is fine zoom
        translateCamera(0, 0, dy * zoomFineSens);
      } else if (me.isSecondaryButtonDown()) {
        //Secondary button is translate object or pan
        vecPos = unProjectDirection(
            mousePosX,
            mousePosY,
            csgScene.getWidth(),
            csgScene.getHeight());
        Point3D translation = vecPos.subtract(vecIni).multiply(distance);
        final double sens = this.translate.getZ() / 400;

        //Selection is null if we didn't pick an object, so pan the camera
        if (selection == null) {
          translateCamera(
              translation.getX() * -1 * sens,
              translation.getY() * sens,
              0
          );
        } else {
          selection.getTransforms().add(new Translate(
              translation.getX() * sens,
              translation.getY() * sens,
              translation.getZ() * sens));
        }

        vecIni = vecPos;
        PickResult pr = me.getPickResult();
        if (pr != null
            && pr.getIntersectedNode() != null
            && pr.getIntersectedNode() == selection) {
          distance = pr.getIntersectedDistance();
        }
      }
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
    });

    csgScene.setOnScroll(event -> translateCamera(0, 0, event.getDeltaY() * zoomSens));
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //Resize the subscene with the borderpane
    csgScene.heightProperty().bind(root.heightProperty());
    csgScene.widthProperty().bind(root.widthProperty());

    //Clip the subscene so it doesn't overlap with other borderpane elements
    final Rectangle csgClip = new Rectangle();
    csgScene.setClip(csgClip);
    csgScene.layoutBoundsProperty().addListener((observableValue, oldBounds, newBounds) -> {
      csgClip.setWidth(newBounds.getWidth());
      csgClip.setHeight(newBounds.getHeight() - 35); //35 is the height of the bottom HBox
    });

    root.setCenter(csgScene);
    root.setId("cadViewerBorderPane");
  }

  /*
     From fx83dfeatures.Camera3D
     http://hg.openjdk.java.net/openjfx/8u-dev/rt/file/5d371a34ddf1/apps/toys/FX8-3DFeatures/src
     /fx83dfeatures/Camera3D.java
    */

  /**
   * Undo scene projection.
   *
   * @param sceneX      Scene x coordinate
   * @param sceneY      Scene y coordinate
   * @param sceneWidth  Scene width
   * @param sceneHeight Scene height
   * @return Un-projected point
   */
  private Point3D unProjectDirection(double sceneX,
                                     double sceneY,
                                     double sceneWidth,
                                     double sceneHeight) {
    double tanHFov = Math.tan(Math.toRadians(camera1.getFieldOfView()) * 0.5f);
    Point3D virtualMouse = new Point3D(
        tanHFov * (2 * sceneX / sceneWidth - 1),
        tanHFov * (2 * sceneY / sceneWidth - sceneHeight / sceneWidth),
        1);

    return localToSceneDirection(virtualMouse).normalize();
  }

  /**
   * Transform a local point to a scene point.
   *
   * @param pt Point to transform
   * @return Transformed point
   */
  private Point3D localToScene(Point3D pt) {
    Point3D res = camera1.localToParentTransformProperty().get().transform(pt);
    if (camera1.getParent() != null) {
      res = camera1.getParent().localToSceneTransformProperty().get().transform(res);
    }
    return res;
  }

  /**
   * Get the un-normalized direction of the local-to-scene transform.
   *
   * @param dir Point to transform
   * @return Direction
   */
  private Point3D localToSceneDirection(Point3D dir) {
    Point3D res = localToScene(dir);
    return res.subtract(localToScene(new Point3D(0, 0, 0)));
  }

  /**
   * Add a MeshView to the scene graph.
   *
   * @param mesh MeshView to add
   * @param csg  CSG object the MeshView is contained in (used for exporting)
   */
  public void addMeshView(MeshView mesh, CSG csg) {
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

        MenuItem exportSTL = new MenuItem("Export as STL");
        exportSTL.setOnAction(event -> {
          FileChooser chooser = new FileChooser();
          File save = chooser.showSaveDialog(root.getScene().getWindow());
          if (save != null) {
            if (!save.getPath().endsWith(".stl")) {
              save = new File(save.getAbsolutePath() + ".stl");
            }

            CSG readyCSG = csg.prepForManufacturing();
            try {
              FileUtils.write(save, readyCSG.toStlString());
            } catch (IOException e) {
              LoggerUtilities.getLogger().log(Level.SEVERE,
                  "Could not write CSG STL String.\n" + Throwables.getStackTraceAsString(e));
            }
          }
        });

        menu.getItems().addAll(wireframe, exportSTL);
        //Need to set the root as mesh.getScene().getWindow() so setAutoHide() works when we
        //right-click somewhere else
        mesh.setOnContextMenuRequested(event ->
            menu.show(mesh.getScene().getWindow(), event.getScreenX(), event.getScreenY()));
      }
    });

    csgGraph.getChildren().add(mesh);
  }

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  public void addMeshesFromCSG(CSG csg) {
    csg.toJavaFXMesh(null).getAsMeshViews().forEach(mesh -> addMeshView(mesh, csg));
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
    cameraXForm1.rotateX(rotX);
    cameraXForm1.rotateY(rotY);
    cameraXForm1.rotateZ(rotZ);
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

  /**
   * Removes all meshes except for the background.
   */
  public void clearMeshes() {
    csgGraph.getChildren().clear();
    csgGraph.getChildren().add(backgroundImage); //Re-add background
  }

  @FXML
  private void onHomeCamera(ActionEvent actionEvent) {
    homeCamera();
  }

  /**
   * Homes the camera rotation and translation.
   */
  public void homeCamera() {
    cameraXForm1.home();
    translate.setX(0);
    translate.setY(0);
    translate.setZ(-800);
  }

  public double getCameraRotateX() {
    return cameraXForm1.getRotX();
  }

  public double getCameraRotateY() {
    return cameraXForm1.getRotY();
  }

  public double getCameraRotateZ() {
    return cameraXForm1.getRotZ();
  }

  public Translate getCameraTranslate() {
    return translate;
  }

  /**
   * Apply rotations iteratively to a group so the camera stays locked to azimuth rotations.
   */
  private static final class XFormCamera extends Group {
    private Rotate rotation;
    private double rotX;
    private double rotY;
    private double rotZ;
    private Transform transform = new Rotate();

    XFormCamera() {
      super();
      home();
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
      rotX = 180;
      rotY = 0;
      rotZ = 180;
      transform = new Rotate(rotX, Rotate.X_AXIS);
      transform = transform.createConcatenation(new Rotate(rotY, Rotate.Y_AXIS));
      transform = transform.createConcatenation(new Rotate(rotZ, Rotate.Z_AXIS));
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