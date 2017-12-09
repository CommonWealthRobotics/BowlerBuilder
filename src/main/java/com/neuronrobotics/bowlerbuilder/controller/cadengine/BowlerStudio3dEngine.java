package com.neuronrobotics.bowlerbuilder.controller.cadengine; //NOPMD

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.imageprovider.VirtualCameraFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Stop;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import org.reactfx.util.FxTimer;

public class BowlerStudio3dEngine extends Pane {

  private final Group axisGroup = new Group();
  private final Group gridGroup = new Group();

  private final XForm world = new XForm();
  private final PerspectiveCamera camera = new PerspectiveCamera(true);

  private final Group root = new Group();
  private final Group lookGroup = new Group();
  private final Group focusGroup = new Group();
  private final Group userGroup = new Group();

  private double mousePosX;
  private double mousePosY;
  private double mouseOldX;
  private double mouseOldY;
  private double mouseDeltaX;
  private double mouseDeltaY;

  private SubScene scene;

  private Group ground;
  private VirtualCameraDevice virtualCam;
  private VirtualCameraMobileBase flyingCamera;
  private Map<CSG, MeshView> csgMap = new HashMap<>();
  private final Map<MeshView, Axis> axisMap = new HashMap<>();
  private CSG selectedCsg;
  private long lastMouseMovementTime = System.currentTimeMillis();

  private TransformNR previousTarget = new TransformNR();

  private long lastSelectedTime = System.currentTimeMillis();

  public BowlerStudio3dEngine() {
    setSubScene(new SubScene(root, 1024, 1024, true, SceneAntialiasing.BALANCED));
    buildScene();
    buildCamera();
    buildAxes(); //NOPMD

    Stop[] stops = null;
    scene.setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, stops));
    handleMouse(scene);
    getChildren().add(scene);
  }

  /**
   * Build the scene. Setup camera angle and add world to the root.
   */
  private void buildScene() {
    world.ry.setAngle(-90);// point z upwards
    world.ry.setAngle(180);// arm out towards user
    getRoot().getChildren().add(world);
  }

  /**
   * Build the camera. Setup the pointer, clips, rotation, and position.
   */
  private void buildCamera() {
    camera.setNearClip(.1);
    camera.setFarClip(100000.0);
    getSubScene().setCamera(camera);

    camera.setRotationAxis(Rotate.Z_AXIS);
    camera.setRotate(180);

    CSG cylinder = new Cylinder(
        0, // Radius at the top
        5, // Radius at the bottom
        20, // Height
        20 // resolution
    ).toCSG().roty(90).setColor(Color.BLACK);
    Group hand = new Group(cylinder.getMesh());
    setVirtualCam(new VirtualCameraDevice(camera, hand));
    VirtualCameraFactory.setFactory(() -> virtualCam);

    try {
      setFlyingCamera(new VirtualCameraMobileBase());
    } catch (Exception e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load VirtualCameraMobileBase.\n" + Throwables.getStackTraceAsString(e));
    }

    moveCamera(
        new TransformNR(0, 0, 0, new RotationNR(90 - 127, 24, 0)),
        0);
  }

  /**
   * Builds the axes.
   */
  private void buildAxes() {
    Thread buildThread = new Thread(() -> {
      try {
        Image ruler = AssetFactory.loadAsset("ruler.png");
        Image groundLocal = AssetFactory.loadAsset("ground.png");
        Affine groundMove = new Affine();
        groundMove.setTx(-groundLocal.getHeight() / 2);
        groundMove.setTy(-groundLocal.getWidth() / 2);

        double scale = 0.25;
        Affine zRuler = new Affine();
        zRuler.setTz(-20 * scale);
        zRuler.appendScale(scale, scale, scale);
        zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);
        zRuler.appendRotation(-90, 0, 0, 0, 0, 0, 1);
        zRuler.appendRotation(90, 0, 0, 0, 0, 1, 0);
        zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);

        Affine yRuler = new Affine();
        yRuler.setTx(-130 * scale);
        yRuler.setTy(-20 * scale);
        yRuler.appendScale(scale, scale, scale);
        yRuler.appendRotation(180, 0, 0, 0, 1, 0, 0);
        yRuler.appendRotation(-90, 0, 0, 0, 0, 0, 1);

        Affine downset = new Affine();
        downset.setTz(0.1);

        Affine xp = new Affine();
        xp.setTx(-20 * scale);
        xp.appendScale(scale, scale, scale);
        xp.appendRotation(180, 0, 0, 0, 1, 0, 0);

        Platform.runLater(() -> {
          ImageView groundView = new ImageView(groundLocal);
          groundView.getTransforms().addAll(groundMove, downset);
          groundView.setOpacity(0.3);

          ImageView zrulerImage = new ImageView(ruler);
          zrulerImage.getTransforms().addAll(zRuler, downset);

          ImageView rulerImage = new ImageView(ruler);
          rulerImage.getTransforms().addAll(xp, downset);

          ImageView yrulerImage = new ImageView(ruler);
          yrulerImage.getTransforms().addAll(yRuler, downset);

          gridGroup.getChildren().addAll(zrulerImage, rulerImage, yrulerImage, groundView);

          Affine groundPlacement = new Affine();
          groundPlacement.setTz(-1);
          ground = new Group();
          ground.getTransforms().add(groundPlacement);
          focusGroup.getChildren().add(getVirtualCam().getCameraFrame());

          gridGroup.getChildren().addAll(new Axis(), ground);
          showAxis();
          axisGroup.getChildren().addAll(focusGroup, userGroup);
          world.getChildren().addAll(lookGroup, axisGroup);
        });
      } catch (Exception e) {
        LoggerUtilities.getLogger().log(Level.SEVERE,
            "Could not load ruler/ground assets for CAD view.\n"
                + Throwables.getStackTraceAsString(e));
      }
    });

    buildThread.setDaemon(true);
    buildThread.setName("Axis builder thread");
    buildThread.start();
  }

  /**
   * Show the axes.
   */
  public void showAxis() {
    Platform.runLater(() -> axisGroup.getChildren().add(gridGroup));
    axisMap.forEach((mesh, axis) -> axis.show());
  }

  /**
   * Hide the axes.
   */
  public void hideAxis() {
    Platform.runLater(() -> axisGroup.getChildren().remove(gridGroup));
    axisMap.forEach((mesh, axis) -> axis.hide());
  }

  /**
   * Attach mouse listeners to the scene.
   *
   * @param scene the scene
   */
  private void handleMouse(SubScene scene) {
    scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
      long lastClickedTimeLocal;
      long offset = 500;

      @Override
      public void handle(MouseEvent event) {
        resetMouseTime(); //NOPMD
        long lastClickedDifference = (System.currentTimeMillis() - lastClickedTimeLocal);
        FxTimer.runLater(Duration.ofMillis(100), () -> {
          long diff = System.currentTimeMillis() - lastSelectedTime; //NOPMD
          // reset only if an object is not being selected
          if (diff > 2000 && lastClickedDifference < offset) {
            cancelSelection(); //NOPMD
          }
        });

        lastClickedTimeLocal = System.currentTimeMillis();
      }
    });

    scene.setOnMousePressed(mouseEvent -> {
      mousePosX = mouseEvent.getSceneX();
      mousePosY = mouseEvent.getSceneY();
      mouseOldX = mouseEvent.getSceneX();
      mouseOldY = mouseEvent.getSceneY();
      resetMouseTime();
    });

    scene.setOnMouseDragged(mouseEvent -> {
      resetMouseTime();
      mouseOldX = mousePosX;
      mouseOldY = mousePosY;
      mousePosX = mouseEvent.getSceneX();
      mousePosY = mouseEvent.getSceneY();
      mouseDeltaX = (mousePosX - mouseOldX);
      mouseDeltaY = (mousePosY - mouseOldY);

      double modifier = 1.0;
      double modifierFactor = 0.1;

      if (mouseEvent.isControlDown()) {
        modifier = 0.1;
      } else if (mouseEvent.isShiftDown()) {
        modifier = 10.0;
      }

      if (mouseEvent.isPrimaryButtonDown()) {
        TransformNR trans = new TransformNR(
            0,
            0,
            0,
            new RotationNR(mouseDeltaY * modifierFactor * modifier * 2.0,
                mouseDeltaX * modifierFactor * modifier * 2.0, 0
            )
        );

        if (mouseEvent.isPrimaryButtonDown()) {
          moveCamera(trans, 0);
        }
      } else if (mouseEvent.isSecondaryButtonDown()) {
        double depth = -100 / getVirtualCam().getZoomDepth();
        moveCamera(
            new TransformNR(
                mouseDeltaX * modifierFactor * modifier * 1 / depth,
                mouseDeltaY * modifierFactor * modifier * 1 / depth,
                0,
                new RotationNR()),
            0);
      }
    });

    scene.addEventHandler(ScrollEvent.ANY, event -> {
      if (ScrollEvent.SCROLL == event.getEventType()) {
        double zoomFactor = -(event.getDeltaY()) * getVirtualCam().getZoomDepth() / 500;
        getVirtualCam().setZoomDepth(getVirtualCam().getZoomDepth() + zoomFactor);
      }
      event.consume();
    });
  }

  /**
   * Move the camera.
   *
   * @param newPose transform to move by
   * @param seconds seconds to move over
   */
  private void moveCamera(TransformNR newPose, double seconds) {
    getFlyingCamera().DriveArc(newPose, seconds);
  }

  /**
   * Select a CSG.
   *
   * @param scg new CSG
   */
  private void setSelectedCsg(CSG scg) {
    if (scg.equals(selectedCsg)) {
      return;
    }

    for (CSG key : getCsgMap().keySet()) {
      Platform.runLater(() ->
          getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor()))); //NOPMD
    }

    lastSelectedTime = System.currentTimeMillis();
    selectedCsg = scg;

    FxTimer.runLater(Duration.ofMillis(20), () ->
        getCsgMap().get(selectedCsg).setMaterial(new PhongMaterial(Color.GOLD)));

    double xCenter = selectedCsg.getMaxX() / 2 + selectedCsg.getMinX() / 2;
    double yCenter = selectedCsg.getMaxY() / 2 + selectedCsg.getMinY() / 2;
    double zCenter = selectedCsg.getMaxZ() / 2 + selectedCsg.getMinZ() / 2;

    TransformNR poseToMove = new TransformNR();
    CSG finalCSG = selectedCsg;
    if (selectedCsg.getMaxX() < 1 || selectedCsg.getMinX() > -1) {
      finalCSG = finalCSG.movex(-xCenter);
      poseToMove.translateX(xCenter);
    }
    if (selectedCsg.getMaxY() < 1 || selectedCsg.getMinY() > -1) {
      finalCSG = finalCSG.movey(-yCenter);
      poseToMove.translateY(yCenter);
    }
    if (selectedCsg.getMaxZ() < 1 || selectedCsg.getMinZ() > -1) {
      finalCSG = finalCSG.movez(-zCenter);
      poseToMove.translateZ(zCenter);
    }

    Affine centering = TransformFactory.nrToAffine(poseToMove);
    // this section keeps the camera oriented the same way to avoid whipping around
    TransformNR rotationOnlyCOmponentOfManipulator
        = TransformFactory.affineToNr(selectedCsg.getManipulator());
    rotationOnlyCOmponentOfManipulator.setX(0);
    rotationOnlyCOmponentOfManipulator.setY(0);
    rotationOnlyCOmponentOfManipulator.setZ(0);
    TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();

    TransformNR startSelectNr = previousTarget.copy();
    TransformNR targetNR;
    if (Math.abs(selectedCsg.getManipulator().getTx()) > 0.1
        || Math.abs(selectedCsg.getManipulator().getTy()) > 0.1
        || Math.abs(selectedCsg.getManipulator().getTz()) > 0.1) {
      targetNR = TransformFactory.affineToNr(selectedCsg.getManipulator());
    } else {
      targetNR = TransformFactory.affineToNr(centering);
    }

    Affine interpolator = new Affine();
    Affine correction = TransformFactory.nrToAffine(reverseRotation);

    Platform.runLater(() -> {
      interpolator.setTx(startSelectNr.getX() - targetNR.getX());
      interpolator.setTy(startSelectNr.getY() - targetNR.getY());
      interpolator.setTz(startSelectNr.getZ() - targetNR.getZ());
      removeAllFocusTransforms();
      focusGroup.getTransforms().add(interpolator);
      if (Math.abs(selectedCsg.getManipulator().getTx()) > 0.1
          || Math.abs(selectedCsg.getManipulator().getTy()) > 0.1
          || Math.abs(selectedCsg.getManipulator().getTz()) > 0.1) {
        focusGroup.getTransforms().add(selectedCsg.getManipulator());
        focusGroup.getTransforms().add(correction);
      } else {
        focusGroup.getTransforms().add(centering);
      }
      focusInterpolate(startSelectNr, targetNR, 0, 30, interpolator);
    });
    resetMouseTime();
  }

  /**
   * Select each CSG in the list.
   *
   * @param selectedCsg list of CSGs to select
   */
  private void setSelectedCsg(List<CSG> selectedCsg) {
    for (int in = 1; in < selectedCsg.size(); in++) {
      MeshView mesh = getCsgMap().get(selectedCsg.get(in));
      if (mesh != null) {
        FxTimer.runLater(Duration.ofMillis(20), () ->
            mesh.setMaterial(new PhongMaterial(Color.GOLD))); //NOPMD
      }
    }

    resetMouseTime();
  }

  /**
   * Select a CSG from the line in the script.
   *
   * @param script script containing CSG source
   * @param lineNumber line number in script
   */
  public void setSelectedCsg(File script, int lineNumber) {
    List<CSG> objsFromScriptLine = new ArrayList<>();

    // check all visible CSGs
    for (CSG checker : getCsgMap().keySet()) {
      for (String trace : checker.getCreationEventStackTraceList()) {
        String[] traceParts = trace.split(":");
        if (traceParts[0]
            .trim()
            .toLowerCase(Locale.US)
            .contains(script.getName()
                .toLowerCase(Locale.US)
                .trim())) {
          try {
            int num = Integer.parseInt(traceParts[1].trim());

            if (num == lineNumber) {
              objsFromScriptLine.add(checker);
            }
          } catch (Exception e) {
            LoggerUtilities.getLogger().log(Level.WARNING,
                "Could not select CSG in script.\n" + Throwables.getStackTraceAsString(e));
          }
        }
      }
    }

    if (!objsFromScriptLine.isEmpty()) {
      setSelectedCsg(objsFromScriptLine.get(0));
      setSelectedCsg(objsFromScriptLine);
    }
  }

  /**
   * De-select the selection.
   */
  private void cancelSelection() {
    for (CSG key : getCsgMap().keySet()) {
      Platform.runLater(() ->
          getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor()))); //NOPMD
    }

    this.selectedCsg = null;
    TransformNR startSelectNr = previousTarget.copy();
    TransformNR targetNR = new TransformNR();
    Affine interpolator = new Affine();
    TransformFactory.nrToAffine(startSelectNr, interpolator);

    Platform.runLater(() -> {
      removeAllFocusTransforms();
      focusGroup.getTransforms().add(interpolator);
      focusInterpolate(startSelectNr, targetNR, 0, 15, interpolator);
    });

    resetMouseTime();
  }

  private void resetMouseTime() {
    this.lastMouseMovementTime = System.currentTimeMillis();
  }

  private void focusInterpolate(TransformNR start,
      TransformNR target,
      int depth,
      int targetDepth,
      Affine interpolator) {

    double depthScale = 1 - (double) depth / (double) targetDepth;
    double sinunsoidalScale = Math.sin(depthScale * (Math.PI / 2));

    double difference = start.getX() - target.getX();

    double xIncrement = difference * sinunsoidalScale;
    double yIncrement = (start.getY() - target.getY()) * sinunsoidalScale;
    double zIncrement = (start.getZ() - target.getZ()) * sinunsoidalScale;

    Platform.runLater(() -> {
      interpolator.setTx(xIncrement);
      interpolator.setTy(yIncrement);
      interpolator.setTz(zIncrement);
    });

    if (depth < targetDepth) {
      FxTimer.runLater(Duration.ofMillis(16), () ->
          focusInterpolate(start, target, depth + 1, targetDepth, interpolator));
    } else {
      Platform.runLater(() -> focusGroup.getTransforms().remove(interpolator));
      previousTarget = target.copy();
      previousTarget.setRotation(new RotationNR());
    }
  }

  private void removeAllFocusTransforms() {
    ObservableList<Transform> allTrans = focusGroup.getTransforms();
    Transform[] toRemove = allTrans.toArray(new Transform[allTrans.size()]);
    Arrays.stream(toRemove).forEach(allTrans::remove);
  }

  public Map<CSG, MeshView> getCsgMap() {
    return csgMap;
  }

  private void setCsgMap(Map<CSG, MeshView> csgMap) {
    this.csgMap = csgMap;
  }

  public long getLastMouseMoveTime() {
    return lastMouseMovementTime;
  }

  /**
   * Gets the camera field of view property.
   *
   * @return the camera field of view property
   */
  public DoubleProperty getCameraFieldOfViewProperty() {
    return camera.fieldOfViewProperty();
  }

  public SubScene getSubScene() {
    return scene;
  }

  private void setSubScene(SubScene scene) {
    this.scene = scene;
  }

  public Group getRoot() {
    return root;
  }

  public VirtualCameraDevice getVirtualCam() {
    return virtualCam;
  }

  private void setVirtualCam(VirtualCameraDevice virtualCam) {
    this.virtualCam = virtualCam;
  }

  public VirtualCameraMobileBase getFlyingCamera() {
    return flyingCamera;
  }

  private void setFlyingCamera(VirtualCameraMobileBase flyingCamera) {
    this.flyingCamera = flyingCamera;
  }

  public CSG getSelectedCsg() {
    return selectedCsg;
  }

  public XForm getWorld() {
    return world;
  }
}
