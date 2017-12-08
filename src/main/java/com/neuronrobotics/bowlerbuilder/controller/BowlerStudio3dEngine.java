package com.neuronrobotics.bowlerbuilder.controller;

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
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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

public class BowlerStudio3dEngine extends JFXPanel {

  private final Group axisGroup = new Group();
  private final Group gridGroup = new Group();

  private final XForm world = new XForm();
  private final PerspectiveCamera camera = new PerspectiveCamera(true);

  private final Group root = new Group();
  private final Group manipulator = new Group();
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
  private VirtualCameraDevice virtualcam;
  private VirtualCameraMobileBase flyingCamera;
  private HashMap<CSG, MeshView> csgMap = new HashMap<>();
  private HashMap<MeshView, Axis> axisMap = new HashMap<>();
  private CSG selectedCsg = null;
  private long lastMosueMovementTime = System.currentTimeMillis();

  private TransformNR perviousTarget = new TransformNR();

  private long lastSelectedTime = System.currentTimeMillis();

  public BowlerStudio3dEngine() {
    setSubScene(new SubScene(getRoot(), 1024, 1024, true, null));
    buildScene();
    buildCamera();
    buildAxes();

    Stop[] stops = null;
    getSubScene().setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, stops));
    Scene s = new Scene(new Group(getSubScene()));
    // handleKeyboard(s);
    handleMouse(getSubScene());

    setScene(s);
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

    CSG cylinder = new Cylinder(0, // Radius at the top
        5, // Radius at the bottom
        20, // Height
        20 // resolution
    ).toCSG().roty(90).setColor(Color.BLACK);

    Group hand = new Group(cylinder.getMesh());

    camera.setNearClip(.1);
    camera.setFarClip(100000.0);
    getSubScene().setCamera(camera);

    camera.setRotationAxis(Rotate.Z_AXIS);
    camera.setRotate(180);

    setVirtualcam(new VirtualCameraDevice(camera, hand));
    VirtualCameraFactory.setFactory(() -> virtualcam);

    try {
      setFlyingCamera(new VirtualCameraMobileBase());
    } catch (Exception e) {
      e.printStackTrace();
    }

    moveCamera(new TransformNR(
        0,
        0,
        0,
        new RotationNR(90 - 127, 24, 0)), 0
    );
  }

  /**
   * Gets the camera field of view property.
   *
   * @return the camera field of view property
   */
  public DoubleProperty getCameraFieldOfViewProperty() {
    return camera.fieldOfViewProperty();
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
        // groundMove.setTz(-3);
        groundMove.setTx(-groundLocal.getHeight() / 2);
        groundMove.setTy(-groundLocal.getWidth() / 2);

        Affine zRuler = new Affine();
        double scale = 0.25;
        // zRuler.setTx(-130*scale);
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

        Affine xp = new Affine();
        Affine downset = new Affine();
        downset.setTz(0.1);
        xp.setTx(-20 * scale);
        xp.appendScale(scale, scale, scale);
        xp.appendRotation(180, 0, 0, 0, 1, 0, 0);

        Platform.runLater(() -> {
          ImageView rulerImage = new ImageView(ruler);
          ImageView yrulerImage = new ImageView(ruler);
          ImageView zrulerImage = new ImageView(ruler);
          ImageView groundView = new ImageView(groundLocal);
          groundView.getTransforms().addAll(groundMove, downset);
          groundView.setOpacity(0.3);
          zrulerImage.getTransforms().addAll(zRuler, downset);
          rulerImage.getTransforms().addAll(xp, downset);
          yrulerImage.getTransforms().addAll(yRuler, downset);
          gridGroup.getChildren().addAll(zrulerImage, rulerImage, yrulerImage, groundView);

          Affine groundPlacment = new Affine();
          groundPlacment.setTz(-1);
          // ground.setOpacity(.5);
          ground = new Group();
          ground.getTransforms().add(groundPlacment);
          focusGroup.getChildren().add(getVirtualcam().getCameraFrame());

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
    for (MeshView a : axisMap.keySet()) {
      axisMap.get(a).show();
    }
  }

  /**
   * Hide the axes.
   */
  public void hideAxis() {
    Platform.runLater(() -> axisGroup.getChildren().remove(gridGroup));
    for (MeshView a : axisMap.keySet()) {
      axisMap.get(a).hide();
    }
  }

  /**
   * Attach mouse listeners to the scene.
   *
   * @param scene the scene
   */
  private void handleMouse(SubScene scene) {

    scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
      long lastClickedTimeLocal = 0;
      long offset = 500;

      @Override
      public void handle(MouseEvent event) {
        resetMouseTime();
        long lastClickedDifference = (System.currentTimeMillis() - lastClickedTimeLocal);
        FxTimer.runLater(Duration.ofMillis(100), () -> {
          long diff = System.currentTimeMillis() - lastSelectedTime;

          if (diff > 2000) {
            // reset only if an object is not being selected
            if (lastClickedDifference < offset) {
              cancelSelection();
            }
          }

        });
        lastClickedTimeLocal = System.currentTimeMillis();
      }

    });

    scene.setOnMousePressed(me -> {
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      mouseOldX = me.getSceneX();
      mouseOldY = me.getSceneY();
      resetMouseTime();
    });

    scene.setOnMouseDragged(me -> {
      resetMouseTime();
      mouseOldX = mousePosX;
      mouseOldY = mousePosY;
      mousePosX = me.getSceneX();
      mousePosY = me.getSceneY();
      mouseDeltaX = (mousePosX - mouseOldX);
      mouseDeltaY = (mousePosY - mouseOldY);

      double modifier = 1.0;
      double modifierFactor = 0.1;

      if (me.isControlDown()) {
        modifier = 0.1;
      } else if (me.isShiftDown()) {
        modifier = 10.0;
      }

      if (me.isPrimaryButtonDown()) {
        TransformNR trans = new TransformNR(
            0,
            0,
            0,
            new RotationNR(mouseDeltaY * modifierFactor * modifier * 2.0,
                mouseDeltaX * modifierFactor * modifier * 2.0, 0
            )
        );

        if (me.isPrimaryButtonDown()) {
          moveCamera(trans, 0);
        }
      } else if (me.isSecondaryButtonDown()) {
        double depth = -100 / getVirtualcam().getZoomDepth();
        moveCamera(new TransformNR(mouseDeltaX * modifierFactor * modifier * 1 / depth,
            mouseDeltaY * modifierFactor * modifier * 1 / depth, 0, new RotationNR()), 0);
      }
    });

    scene.addEventHandler(ScrollEvent.ANY, t -> {
      if (ScrollEvent.SCROLL == t.getEventType()) {
        double zoomFactor = -(t.getDeltaY()) * getVirtualcam().getZoomDepth() / 500;
        getVirtualcam().setZoomDepth(getVirtualcam().getZoomDepth() + zoomFactor);
      }
      t.consume();
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
   * Gets the sub scene.
   *
   * @return the sub scene
   */
  public SubScene getSubScene() {
    return scene;
  }

  /**
   * Sets the sub scene.
   *
   * @param scene the new sub scene
   */
  private void setSubScene(SubScene scene) {
    this.scene = scene;
  }

  /**
   * Gets the root.
   *
   * @return the root
   */
  public Group getRoot() {
    return root;
  }

  /**
   * Removes the arm.
   */
  public void removeArm() {
    world.getChildren().remove(manipulator);
  }

  public VirtualCameraDevice getVirtualcam() {
    return virtualcam;
  }

  private void setVirtualcam(VirtualCameraDevice virtualcam) {
    this.virtualcam = virtualcam;
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

  /**
   * Set the selected CSG.
   *
   * @param scg new CSG
   */
  private void setSelectedCsg(CSG scg) {
    if (scg == selectedCsg) {
      return;
    }

    for (CSG key : getCsgMap().keySet()) {
      Platform.runLater(() -> getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor())));
    }

    lastSelectedTime = System.currentTimeMillis();
    selectedCsg = scg;

    FxTimer.runLater(java.time.Duration.ofMillis(20), () ->
        getCsgMap().get(selectedCsg).setMaterial(new PhongMaterial(Color.GOLD)));

    double xcenter = selectedCsg.getMaxX() / 2 + selectedCsg.getMinX() / 2;
    double ycenter = selectedCsg.getMaxY() / 2 + selectedCsg.getMinY() / 2;
    double zcenter = selectedCsg.getMaxZ() / 2 + selectedCsg.getMinZ() / 2;

    TransformNR poseToMove = new TransformNR();
    CSG finalCSG = selectedCsg;
    if (selectedCsg.getMaxX() < 1 || selectedCsg.getMinX() > -1) {
      finalCSG = finalCSG.movex(-xcenter);
      poseToMove.translateX(xcenter);
    }
    if (selectedCsg.getMaxY() < 1 || selectedCsg.getMinY() > -1) {
      finalCSG = finalCSG.movey(-ycenter);
      poseToMove.translateY(ycenter);
    }
    if (selectedCsg.getMaxZ() < 1 || selectedCsg.getMinZ() > -1) {
      finalCSG = finalCSG.movez(-zcenter);
      poseToMove.translateZ(zcenter);
    }

    Affine centering = TransformFactory.nrToAffine(poseToMove);
    // this section keeps the camera oriented the same way to avoid whipping around
    TransformNR rotationOnlyCOmponentOfManipulator
        = TransformFactory.affineToNr(selectedCsg.getManipulator());
    rotationOnlyCOmponentOfManipulator.setX(0);
    rotationOnlyCOmponentOfManipulator.setY(0);
    rotationOnlyCOmponentOfManipulator.setZ(0);
    TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();

    TransformNR startSelectNr = perviousTarget.copy();
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
   * Select the list of CSGs.
   *
   * @param selectedCsg list of CSGs to select
   */
  private void setSelectedCsg(List<CSG> selectedCsg) {
    for (int in = 1; in < selectedCsg.size(); in++) {
      MeshView mesh = getCsgMap().get(selectedCsg.get(in));
      if (mesh != null) {
        FxTimer.runLater(java.time.Duration.ofMillis(20), () ->
            mesh.setMaterial(new PhongMaterial(Color.GOLD)));
      }
    }

    resetMouseTime();
  }

  /**
   * De-select the selection.
   */
  private void cancelSelection() {
    for (CSG key : getCsgMap().keySet()) {
      Platform.runLater(() -> getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor())));
    }

    this.selectedCsg = null;
    TransformNR startSelectNr = perviousTarget.copy();
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
    this.lastMosueMovementTime = System.currentTimeMillis();
  }

  private void focusInterpolate(TransformNR start,
                                TransformNR target,
                                int depth,
                                int targetDepth,
                                Affine interpolator) {

    double depthScale = 1 - (double) depth / (double) targetDepth;
    double sinunsoidalScale = Math.sin(depthScale * (Math.PI / 2));

    double difference = start.getX() - target.getX();

    double xIncrement = (difference * sinunsoidalScale);
    double yIncrement = ((start.getY() - target.getY()) * sinunsoidalScale);
    double zIncrement = ((start.getZ() - target.getZ()) * sinunsoidalScale);

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
      perviousTarget = target.copy();
      perviousTarget.setRotation(new RotationNR());
    }
  }

  private void removeAllFocusTransforms() {
    ObservableList<Transform> allTrans = focusGroup.getTransforms();
    Transform[] toRemove = allTrans.toArray(new Transform[0]);
    Arrays.stream(toRemove).forEach(allTrans::remove);
  }

  public HashMap<CSG, MeshView> getCsgMap() {
    return csgMap;
  }

  private void setCsgMap(HashMap<CSG, MeshView> csgMap) {
    this.csgMap = csgMap;
  }

  private void setSelectedCsg(File script, int lineNumber) {
    List<CSG> objsFromScriptLine = new ArrayList<>();

    // check all visible CSGs
    for (CSG checker : getCsgMap().keySet()) {
      for (String trace : checker.getCreationEventStackTraceList()) {
        String[] traceParts = trace.split(":");
        if (traceParts[0].trim().toLowerCase().contains(script.getName().toLowerCase().trim())) {
          try {
            int num = Integer.parseInt(traceParts[1].trim());

            if (num == lineNumber) {
              objsFromScriptLine.add(checker);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    if (objsFromScriptLine.size() > 0) {
      setSelectedCsg(objsFromScriptLine.get(0));
      setSelectedCsg(objsFromScriptLine);
    }
  }

  public long getLastMouseMoveTime() {
    return lastMosueMovementTime;
  }

}
