/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine; // NOPMD

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.util.CsgParser;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesServiceFactory;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraMobileBase;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.XForm;
import com.neuronrobotics.bowlerbuilder.view.cadengine.element.Axis3D;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.imageprovider.VirtualCameraFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.Parameter;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Stop;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javax.annotation.Nonnull;
import org.apache.commons.io.FileUtils;
import org.reactfx.util.FxTimer;

public class BowlerCadEngine extends Pane implements CadEngine {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(BowlerCadEngine.class.getSimpleName());

  private final Group axisGroup = new Group();
  private final Group gridGroup = new Group();

  private final XForm world = new XForm();
  private final PerspectiveCamera camera = new PerspectiveCamera(true);

  private final CsgParser csgParser;

  private final Group root = new Group();
  private final Group lookGroup = new Group();
  private final Group focusGroup = new Group();
  private final Group meshViewGroup = new Group();
  private final Group hand = new Group();
  private final Map<String, MeshView> csgNameMap = new ConcurrentHashMap<>();
  private final Map<MeshView, Axis3D> axisMap = new ConcurrentHashMap<>();
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
  private TransformNR defaultCameraView;
  private Map<CSG, MeshView> csgMap = new ConcurrentHashMap<>();
  private CSG selectedCsg;
  private long lastMouseMovementTime = System.currentTimeMillis();

  private TransformNR previousTarget = new TransformNR();

  private long lastSelectedTime = System.currentTimeMillis();

  private final BooleanProperty axisShowing;
  private final BooleanProperty handShowing;

  /**
   * CAD Engine from BowlerStudio.
   *
   * @param csgParser {@link CsgParser}
   * @param preferencesServiceFactory {@link PreferencesServiceFactory}
   */
  @Inject
  public BowlerCadEngine(
      @Nonnull final CsgParser csgParser,
      @Nonnull final PreferencesServiceFactory preferencesServiceFactory) {
    super();
    this.csgParser = csgParser;

    axisShowing = new SimpleBooleanProperty(true);
    handShowing = new SimpleBooleanProperty(true);

    final PreferencesService preferencesService =
        preferencesServiceFactory.create("BowlerCadEngine");
    preferencesService.load();
    final Boolean shouldAA = preferencesService.get("CAD Engine Antialiasing", true);

    if (shouldAA) {
      setSubScene(new SubScene(root, 1024, 1024, true, SceneAntialiasing.BALANCED));
    } else {
      setSubScene(new SubScene(root, 1024, 1024, true, SceneAntialiasing.DISABLED));
    }
    buildScene();
    buildCamera();
    buildAxes();

    scene.setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, (Stop[]) null));
    handleMouse(scene);
    getChildren().add(scene);

    // Clip view so it doesn'translate overlap with anything
    final Rectangle engineClip = new Rectangle();
    setClip(engineClip);
    layoutBoundsProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              engineClip.setWidth(newValue.getWidth());
              engineClip.setHeight(newValue.getHeight());
            });

    axisShowing.addListener(
        (observableValue, oldVal, newVal) -> {
          if (newVal) {
            showAxis();
          } else {
            hideAxis();
          }
        });

    handShowing.addListener(
        (observableValue, oldVal, newVal) -> {
          if (newVal) {
            showHand();
          } else {
            hideHand();
          }
        });
  }

  /** Build the scene. Setup camera angle and add world to the root. */
  private void buildScene() {
    world.rotY.setAngle(-90); // point z upwards
    world.rotY.setAngle(180); // arm out towards user
    root.getChildren().add(world);
  }

  /** Build the camera. Setup the pointer, clips, rotation, and position. */
  private void buildCamera() {
    camera.setNearClip(.1);
    camera.setFarClip(100000.0);
    scene.setCamera(camera);

    camera.setRotationAxis(Rotate.Z_AXIS);
    camera.setRotate(180);

    final CSG cylinder =
        new Cylinder(
                0, // Radius at the top
                5, // Radius at the bottom
                20, // Height
                20 // resolution
                )
            .toCSG()
            .roty(90)
            .setColor(Color.BLACK);
    hand.getChildren().add(cylinder.getMesh());
    virtualCam = new VirtualCameraDevice(camera, hand);
    VirtualCameraFactory.setFactory(() -> virtualCam);

    try {
      flyingCamera =
          new VirtualCameraMobileBase(
              "<root>\n"
                  + "<mobilebase>\n"
                  + "<driveType>none</driveType>\n"
                  + "<name>FlyingCamera</name>\n"
                  + "<appendage>\n"
                  + "<name>BoomArm</name>\n"
                  + "<link>\n"
                  + "\t<name>boom</name>\n"
                  + "\t<deviceName>"
                  + virtualCam.hashCode()
                  + "</deviceName>\n"
                  + "\t<type>camera</type>\n"
                  + "\t<index>0</index>\n"
                  + "\t\n"
                  + "\t<scale>1</scale>\n"
                  + "\t<upperLimit>255.0</upperLimit>\n"
                  + "\t<lowerLimit>0.0</lowerLimit>\n"
                  + "\t<upperVelocity>1.0E8</upperVelocity>\n"
                  + "\t<lowerVelocity>-1.0E8</lowerVelocity>\n"
                  + "\t<staticOffset>0</staticOffset>\n"
                  + "\t<isLatch>false</isLatch>\n"
                  + "\t<indexLatch>0</indexLatch>\n"
                  + "\t<isStopOnLatch>false</isStopOnLatch>\n"
                  + "\t<homingTPS>10000000</homingTPS>\n"
                  + "\t\n"
                  + "\t<DHParameters>\n"
                  + "\t\t<Delta>0</Delta>\n"
                  + "\t\t<Theta>0.0</Theta>\n"
                  + "\t\t<Radius>0</Radius>\n"
                  + "\t\t<Alpha>0</Alpha>\n"
                  + "\t</DHParameters>\n"
                  + "\n"
                  + "</link>\n"
                  + "\n"
                  + "<ZframeToRAS>\t\n"
                  + "<x>0.0</x>\n"
                  + "\t<y>0.0</y>\n"
                  + "\t<z>0.0</z>\n"
                  + "\t<rotw>1.0</rotw>\n"
                  + "\t<rotx>0.0</rotx>\n"
                  + "\t<roty>0.0</roty>\n"
                  + "\t<rotz>0.0</rotz>\n"
                  + "</ZframeToRAS>\n"
                  + "\n"
                  + "<baseToZframe>\n"
                  + "\t<x>0.0</x>\n"
                  + "\t<y>0.0</y>\n"
                  + "\t<z>0.0</z>\n"
                  + "\t<rotw>1.0</rotw>\n"
                  + "\t<rotx>0.0</rotx>\n"
                  + "\t<roty>0.0</roty>\n"
                  + "\t<rotz>0.0</rotz>\n"
                  + "</baseToZframe>\n"
                  + "\n"
                  + "</appendage>\n"
                  + "\n"
                  + "<ZframeToRAS>\n"
                  + "\t<x>0.0</x>\n"
                  + "\t<y>0.0</y>\n"
                  + "\t<z>0.0</z>\n"
                  + "\t<rotw>1.0</rotw>\n"
                  + "\t<rotx>0.0</rotx>\n"
                  + "\t<roty>0.0</roty>\n"
                  + "\t<rotz>0.0</rotz>\n"
                  + "</ZframeToRAS>\n"
                  + "\n"
                  + "<baseToZframe>\n"
                  + "\t<x>0.0</x>\n"
                  + "\t<y>0.0</y>\n"
                  + "\t<z>0.0</z>\n"
                  + "\t<rotw>1.0</rotw>\n"
                  + "\t<rotx>0.0</rotx>\n"
                  + "\t<roty>0.0</roty>\n"
                  + "\t<rotz>0.0</rotz>\n"
                  + "</baseToZframe>\n"
                  + "\n"
                  + "</mobilebase>\n"
                  + "\n"
                  + "</root>");
    } catch (final Exception e) {
      LOGGER.log(
          Level.SEVERE,
          "Could not load VirtualCameraMobileBase.\n" + Throwables.getStackTraceAsString(e));
    }

    moveCamera(new TransformNR(0, 0, 0, new RotationNR(90 - 127, 24, 0)), 0);

    defaultCameraView = flyingCamera.getFiducialToGlobalTransform();
  }

  /** Builds the axes. */
  private void buildAxes() {
    try {
      final Image ruler = AssetFactory.loadAsset("ruler.png");
      final Image groundLocal = AssetFactory.loadAsset("ground.png");
      final Affine groundMove = new Affine();
      groundMove.setTx(-groundLocal.getHeight() / 2);
      groundMove.setTy(-groundLocal.getWidth() / 2);

      final double scale = 0.25;
      final Affine zRuler = new Affine();
      zRuler.setTz(-20 * scale);
      zRuler.appendScale(scale, scale, scale);
      zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);
      zRuler.appendRotation(-90, 0, 0, 0, 0, 0, 1);
      zRuler.appendRotation(90, 0, 0, 0, 0, 1, 0);
      zRuler.appendRotation(-180, 0, 0, 0, 1, 0, 0);

      final Affine yRuler = new Affine();
      yRuler.setTx(-130 * scale);
      yRuler.setTy(-20 * scale);
      yRuler.appendScale(scale, scale, scale);
      yRuler.appendRotation(180, 0, 0, 0, 1, 0, 0);
      yRuler.appendRotation(-90, 0, 0, 0, 0, 0, 1);

      final Affine downset = new Affine();
      downset.setTz(0.1);

      final Affine xRuler = new Affine();
      xRuler.setTx(-20 * scale);
      xRuler.appendScale(scale, scale, scale);
      xRuler.appendRotation(180, 0, 0, 0, 1, 0, 0);

      Platform.runLater(
          () -> {
            final ImageView groundView = new ImageView(groundLocal);
            groundView.getTransforms().addAll(groundMove, downset);
            groundView.setOpacity(0.3);

            final ImageView zrulerImage = new ImageView(ruler);
            zrulerImage.getTransforms().addAll(zRuler, downset);

            final ImageView rulerImage = new ImageView(ruler);
            rulerImage.getTransforms().addAll(xRuler, downset);

            final ImageView yrulerImage = new ImageView(ruler);
            yrulerImage.getTransforms().addAll(yRuler, downset);

            gridGroup.getChildren().addAll(zrulerImage, rulerImage, yrulerImage, groundView);

            final Affine groundPlacement = new Affine();
            groundPlacement.setTz(-1);
            ground = new Group();
            ground.getTransforms().add(groundPlacement);
            focusGroup.getChildren().add(getVirtualCam().getCameraFrame());

            gridGroup.getChildren().addAll(new Axis3D(), ground);
            showAxis();
            axisGroup.getChildren().addAll(focusGroup, meshViewGroup);
            world.getChildren().addAll(lookGroup, axisGroup);
          });
    } catch (final Exception e) {
      LOGGER.log(
          Level.WARNING,
          "Could not load ruler/ground assets for CAD view.\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  /** Show the axes. */
  private void showAxis() {
    Platform.runLater(() -> axisGroup.getChildren().add(gridGroup));
    axisMap.forEach((mesh, axis3D) -> axis3D.show());
  }

  /** Hide the axes. */
  private void hideAxis() {
    Platform.runLater(() -> axisGroup.getChildren().remove(gridGroup));
    axisMap.forEach((mesh, axis3D) -> axis3D.hide());
  }

  private void showHand() {
    hand.setVisible(true);
  }

  private void hideHand() {
    hand.setVisible(false);
  }

  /**
   * Attach mouse listeners to the scene.
   *
   * @param scene the scene
   */
  private void handleMouse(@Nonnull final SubScene scene) {
    scene.setOnMouseClicked(
        new EventHandler<MouseEvent>() {
          private long lastClickedTimeLocal;
          private static final long OFFSET = 500;

          @Override
          public void handle(final MouseEvent event) {
            resetMouseTime(); // NOPMD
            final long lastClickedDifference = System.currentTimeMillis() - lastClickedTimeLocal;
            FxTimer.runLater(
                Duration.ofMillis(100),
                () -> {
                  final long diff = System.currentTimeMillis() - lastSelectedTime; // NOPMD
                  // reset only if an object is not being selected
                  if (diff > 2000 && lastClickedDifference < OFFSET) {
                    cancelSelection(); // NOPMD
                  }
                });

            lastClickedTimeLocal = System.currentTimeMillis();
          }
        });

    scene.setOnMousePressed(
        mouseEvent -> {
          mouseOldX = mousePosX;
          mouseOldY = mousePosY;
          mousePosX = mouseEvent.getSceneX();
          mousePosY = mouseEvent.getSceneY();
          resetMouseTime();
        });

    scene.setOnMouseDragged(
        mouseEvent -> {
          resetMouseTime();
          mouseOldX = mousePosX;
          mouseOldY = mousePosY;
          mousePosX = mouseEvent.getSceneX();
          mousePosY = mouseEvent.getSceneY();
          mouseDeltaX = mousePosX - mouseOldX;
          mouseDeltaY = mousePosY - mouseOldY;

          double modifier = 1.0;
          final double modifierFactor = 0.1;

          if (mouseEvent.isControlDown()) {
            modifier = 0.1;
          } else if (mouseEvent.isShiftDown()) {
            modifier = 10.0;
          }

          if (mouseEvent.isPrimaryButtonDown()) {
            final TransformNR trans =
                new TransformNR(
                    0,
                    0,
                    0,
                    new RotationNR(
                        mouseDeltaY * modifierFactor * modifier * 2.0,
                        mouseDeltaX * modifierFactor * modifier * 2.0,
                        0));

            if (mouseEvent.isPrimaryButtonDown()) {
              moveCamera(trans, 0);
            }
          } else if (mouseEvent.isSecondaryButtonDown()) {
            final double depth = -100 / getVirtualCam().getZoomDepth();
            moveCamera(
                new TransformNR(
                    mouseDeltaX * modifierFactor * modifier * 1 / depth,
                    mouseDeltaY * modifierFactor * modifier * 1 / depth,
                    0,
                    new RotationNR()),
                0);
          }
        });

    scene.addEventHandler(
        ScrollEvent.ANY,
        event -> {
          if (ScrollEvent.SCROLL == event.getEventType()) {
            final double zoomFactor = -(event.getDeltaY()) * getVirtualCam().getZoomDepth() / 500;
            virtualCam.setZoomDepth(getVirtualCam().getZoomDepth() + zoomFactor);
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
  private void moveCamera(@Nonnull final TransformNR newPose, final double seconds) {
    flyingCamera.DriveArc(newPose, seconds);
  }

  /** Home the camera to its default view. */
  @Override
  public void homeCamera() {
    flyingCamera.setGlobalToFiducialTransform(defaultCameraView);
    virtualCam.setZoomDepth(VirtualCameraDevice.getDefaultZoomDepth());
    flyingCamera.updatePositions();
  }

  /** De-select the selection. */
  private void cancelSelection() {
    for (final CSG key : getCsgMap().keySet()) {
      Platform.runLater(
          () -> getCsgMap().get(key).setMaterial(new PhongMaterial(key.getColor()))); // NOPMD
    }

    this.selectedCsg = null; // NOPMD
    final TransformNR startSelectNr = previousTarget.copy();
    final TransformNR targetNR = new TransformNR();
    final Affine interpolator = new Affine();
    TransformFactory.nrToAffine(startSelectNr, interpolator);

    Platform.runLater(
        () -> {
          removeAllFocusTransforms();
          focusGroup.getTransforms().add(interpolator);
          focusInterpolate(startSelectNr, targetNR, 0, 15, interpolator);
        });

    resetMouseTime();
  }

  private void resetMouseTime() {
    this.lastMouseMovementTime = System.currentTimeMillis();
  }

  private void focusInterpolate(
      @Nonnull final TransformNR start,
      @Nonnull final TransformNR target,
      final int depth,
      final int targetDepth,
      @Nonnull final Affine interpolator) {

    final double depthScale = 1 - (double) depth / (double) targetDepth;
    final double sinunsoidalScale = Math.sin(depthScale * (Math.PI / 2));

    final double difference = start.getX() - target.getX();

    final double xIncrement = difference * sinunsoidalScale;
    final double yIncrement = (start.getY() - target.getY()) * sinunsoidalScale;
    final double zIncrement = (start.getZ() - target.getZ()) * sinunsoidalScale;

    Platform.runLater(
        () -> {
          interpolator.setTx(xIncrement);
          interpolator.setTy(yIncrement);
          interpolator.setTz(zIncrement);
        });

    if (depth < targetDepth) {
      FxTimer.runLater(
          Duration.ofMillis(16),
          () -> focusInterpolate(start, target, depth + 1, targetDepth, interpolator));
    } else {
      Platform.runLater(() -> focusGroup.getTransforms().remove(interpolator));
      previousTarget = target.copy();
      previousTarget.setRotation(new RotationNR());
    }
  }

  private void removeAllFocusTransforms() {
    final ObservableList<Transform> allTrans = focusGroup.getTransforms();
    final Transform[] toRemove = allTrans.toArray(new Transform[0]);
    Arrays.stream(toRemove).forEach(allTrans::remove);
  }

  @Override
  public Map<CSG, MeshView> getCsgMap() {
    return csgMap;
  }

  private void setCsgMap(@Nonnull final Map<CSG, MeshView> csgMap) {
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

  private void setSubScene(final SubScene scene) {
    this.scene = scene;
  }

  public VirtualCameraDevice getVirtualCam() {
    return virtualCam;
  }

  private void setVirtualCam(@Nonnull final VirtualCameraDevice virtualCam) {
    this.virtualCam = virtualCam;
  }

  public VirtualCameraMobileBase getFlyingCamera() {
    return flyingCamera;
  }

  private void setFlyingCamera(@Nonnull final VirtualCameraMobileBase flyingCamera) {
    this.flyingCamera = flyingCamera;
  }

  public CSG getSelectedCsg() {
    return selectedCsg;
  }

  /**
   * Select all CSGs from the line in the script.
   *
   * @param script script containing CSG source
   * @param lineNumber line number in script
   */
  @Override
  public void setSelectedCsg(@Nonnull final File script, final int lineNumber) {
    Platform.runLater(
        () -> {
          final Collection<CSG> csgs =
              csgParser.parseCsgFromSource(script.getName(), lineNumber, csgMap);

          lastSelectedTime = System.currentTimeMillis();

          if (csgs.size() == 1) {
            selectCSG(csgs.iterator().next(), csgMap);
          } else {
            selectCSGs(csgs);
          }

          resetMouseTime();
        });
  }

  /**
   * Select all CSGs in the collection.
   *
   * @param selection CSGs to select
   */
  @Override
  public void selectCSGs(@Nonnull final Collection<CSG> selection) {
    selection.forEach(
        csg -> {
          final MeshView meshView = csgMap.get(csg);
          if (meshView != null) {
            FxTimer.runLater(
                Duration.ofMillis(20), () -> meshView.setMaterial(new PhongMaterial(Color.GOLD)));
          }
        });
  }

  /**
   * Select a CSG and pan the camera to that CSG.
   *
   * @param selection CSG to select
   * @param csgMap map containing CSGs MeshViews
   */
  private void selectCSG(@Nonnull final CSG selection, @Nonnull final Map<CSG, MeshView> csgMap) {
    if (selection.equals(selectedCsg)) {
      return;
    }

    csgMap
        .keySet()
        .forEach(
            key ->
                Platform.runLater(
                    () -> csgMap.get(key).setMaterial(new PhongMaterial(key.getColor()))));

    lastSelectedTime = System.currentTimeMillis();
    selectedCsg = selection;

    FxTimer.runLater(
        Duration.ofMillis(20),
        () -> getCsgMap().get(selectedCsg).setMaterial(new PhongMaterial(Color.GOLD)));

    final double xCenter = selectedCsg.getCenterX();
    final double yCenter = selectedCsg.getCenterY();
    final double zCenter = selectedCsg.getCenterZ();

    final TransformNR poseToMove = new TransformNR();

    if (selectedCsg.getMaxX() < 1 || selectedCsg.getMinX() > -1) {
      poseToMove.translateX(xCenter);
    }

    if (selectedCsg.getMaxY() < 1 || selectedCsg.getMinY() > -1) {
      poseToMove.translateY(yCenter);
    }

    if (selectedCsg.getMaxZ() < 1 || selectedCsg.getMinZ() > -1) {
      poseToMove.translateZ(zCenter);
    }

    final Affine centering = TransformFactory.nrToAffine(poseToMove);
    // this section keeps the camera oriented the same way to avoid whipping around
    final TransformNR rotationOnlyCOmponentOfManipulator =
        TransformFactory.affineToNr(selectedCsg.getManipulator());
    rotationOnlyCOmponentOfManipulator.setX(0);
    rotationOnlyCOmponentOfManipulator.setY(0);
    rotationOnlyCOmponentOfManipulator.setZ(0);
    final TransformNR reverseRotation = rotationOnlyCOmponentOfManipulator.inverse();

    final TransformNR startSelectNr = previousTarget.copy();
    final TransformNR targetNR;

    if (checkManipulator()) {
      targetNR = TransformFactory.affineToNr(selectedCsg.getManipulator());
    } else {
      targetNR = TransformFactory.affineToNr(centering);
    }

    final Affine interpolator = new Affine();
    final Affine correction = TransformFactory.nrToAffine(reverseRotation);

    Platform.runLater(
        () -> {
          interpolator.setTx(startSelectNr.getX() - targetNR.getX());
          interpolator.setTy(startSelectNr.getY() - targetNR.getY());
          interpolator.setTz(startSelectNr.getZ() - targetNR.getZ());
          removeAllFocusTransforms();
          focusGroup.getTransforms().add(interpolator);
          if (checkManipulator()) {
            focusGroup.getTransforms().add(selectedCsg.getManipulator());
            focusGroup.getTransforms().add(correction);
          } else {
            focusGroup.getTransforms().add(centering);
          }
          focusInterpolate(startSelectNr, targetNR, 0, 30, interpolator);
        });
  }

  private boolean checkManipulator() {
    return Math.abs(selectedCsg.getManipulator().getTx()) > 0.1
        || Math.abs(selectedCsg.getManipulator().getTy()) > 0.1
        || Math.abs(selectedCsg.getManipulator().getTz()) > 0.1;
  }

  /**
   * Add a CSG to the scene graph.
   *
   * @param csg CSG to add
   */
  @Override
  public void addCSG(@Nonnull final CSG csg) {
    final MeshView mesh = csg.getMesh();
    mesh.setMaterial(new PhongMaterial(csg.getColor()));
    mesh.setDepthTest(DepthTest.ENABLE);
    mesh.setCullFace(CullFace.BACK);

    if (csg.getName() != null
        && !"".equals(csg.getName())
        && csgNameMap.containsKey(csg.getName())) {
      mesh.setDrawMode(csgNameMap.get(csg.getName()).getDrawMode());
    } else {
      mesh.setDrawMode(DrawMode.FILL);
    }

    mesh.setOnMouseClicked(
        mouseEvent -> {
          if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (mouseOldX == mouseEvent.getSceneX() && mouseOldY == mouseEvent.getSceneY()) {
              selectCSG(csg, csgMap);
            }
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = mouseEvent.getSceneX();
            mousePosY = mouseEvent.getSceneY();
          } else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
            final ContextMenu menu = new ContextMenu();
            menu.setAutoHide(true);

            // Wireframe/Solid draw toggle
            final MenuItem wireframe;

            // Set the title of the MenuItem to the opposite of the current draw
            if (mesh.getDrawMode().equals(DrawMode.LINE)) {
              wireframe = new MenuItem("Show As Solid");
            } else {
              wireframe = new MenuItem("Show As Wireframe");
            }

            // Set the onAction of the MenuItem to flip the draw state
            wireframe.setOnAction(
                actionEvent -> {
                  if (mesh.getDrawMode().equals(DrawMode.FILL)) {
                    mesh.setDrawMode(DrawMode.LINE);
                    wireframe.setText("Show As Solid");
                  } else {
                    mesh.setDrawMode(DrawMode.FILL);
                    wireframe.setText("Show As Wireframe");
                  }
                });

            final Set<String> params = csg.getParameters();
            if (params != null) {
              final Menu parameters = new Menu("Parameters");
              params.forEach(
                  key -> {
                    // Regenerate all objects if their parameters have changed
                    final Runnable regenerateObjects =
                        () -> {
                          // Get the set of objects to check for
                          // regeneration after the initial
                          // regeneration
                          // cycle
                          final Set<CSG> objects = getCsgMap().keySet();

                          // Hide the menu because the parameter is done
                          // being changed
                          menu.hide();

                          fireRegenerate(key, objects);
                          resetMouseTime();
                        };

                    final Parameter param = CSGDatabase.get(key);
                    csg.setParameterIfNull(key);

                    if (param instanceof LengthParameter) {
                      final LengthParameter lengthParameter = (LengthParameter) param;

                      final EngineeringUnitsSliderWidget widget =
                          new EngineeringUnitsSliderWidget(
                              new EngineeringUnitsChangeListener() {
                                @Override
                                public void onSliderMoving(
                                    final EngineeringUnitsSliderWidget sliderWidget,
                                    final double newAngleDegrees) {
                                  try {
                                    csg.setParameterNewValue(key, newAngleDegrees);
                                  } catch (final Exception e) {
                                    LOGGER.log(
                                        Level.SEVERE, // NOPMD
                                        "Could not set new parameter value.\n"
                                            + Throwables.getStackTraceAsString(e));
                                  }
                                }

                                @Override
                                public void onSliderDoneMoving(
                                    final EngineeringUnitsSliderWidget sliderWidget,
                                    final double newAngleDegrees) {
                                  regenerateObjects.run();
                                }
                              },
                              Double.parseDouble(lengthParameter.getOptions().get(1)),
                              Double.parseDouble(lengthParameter.getOptions().get(0)),
                              lengthParameter.getMM(),
                              400,
                              key);

                      final CustomMenuItem customMenuItem = new CustomMenuItem(widget);
                      customMenuItem.setHideOnClick(false); // Regen will hide the menu
                      parameters.getItems().add(customMenuItem);
                    } else {
                      if (param != null) {
                        final Menu paramTypes =
                            new Menu(param.getName() + " " + param.getStrValue());

                        param
                            .getOptions()
                            .forEach(
                                option -> {
                                  final MenuItem customMenuItem = new MenuItem(option);
                                  customMenuItem.setOnAction(
                                      event -> {
                                        param.setStrValue(option);
                                        CSGDatabase.get(param.getName()).setStrValue(option);
                                        CSGDatabase.getParamListeners(param.getName())
                                            .forEach(
                                                listener ->
                                                    listener.parameterChanged(
                                                        param.getName(), param));
                                        regenerateObjects.run();
                                      });

                                  paramTypes.getItems().add(customMenuItem);
                                });

                        parameters.getItems().add(paramTypes);
                      }
                    }
                  });

              menu.getItems().add(parameters);
            }

            final MenuItem exportSTL = new MenuItem("Export as STL");
            exportSTL.setOnAction(
                event -> {
                  final FileChooser chooser = new FileChooser();
                  File save = chooser.showSaveDialog(root.getScene().getWindow());
                  if (save != null) {
                    if (!save.getPath().endsWith(".stl")) {
                      save = new File(save.getAbsolutePath() + ".stl");
                    }

                    final CSG readyCSG = csg.prepForManufacturing();
                    try {
                      FileUtils.write(save, readyCSG.toStlString());
                    } catch (final IOException e) {
                      LOGGER.log(
                          Level.SEVERE,
                          "Could not write CSG STL String.\n"
                              + Throwables.getStackTraceAsString(e));
                    }
                  }
                });

            menu.getItems().addAll(wireframe, exportSTL);
            // Need to set the root as mesh.getScene().getWindow() so setAutoHide()
            // works when we
            // right-click somewhere else
            mesh.setOnContextMenuRequested(
                event ->
                    menu.show(mesh.getScene().getWindow(), event.getScreenX(), event.getScreenY()));
          }
        });

    Platform.runLater(
        () -> {
          try {
            meshViewGroup.getChildren().add(mesh);
          } catch (final IllegalArgumentException e) {
            LOGGER.warning("Possible duplicate child added to CAD engine.");
            LOGGER.fine(Throwables.getStackTraceAsString(e));
          }
        });
    csgMap.put(csg, mesh);
    csgNameMap.put(csg.getName(), mesh);
    LOGGER.log(Level.FINE, "Added CSG with name: " + csg.getName());
  }

  @Override
  public void addAllCSGs(@Nonnull final CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addCSG);
  }

  @Override
  public void addAllCSGs(@Nonnull final Collection<CSG> csgs) {
    csgs.forEach(this::addCSG);
  }

  @Override
  public void clearMeshes() {
    meshViewGroup.getChildren().clear();
    csgMap.clear();
  }

  @Override
  public BooleanProperty axisShowingProperty() {
    return axisShowing;
  }

  @Override
  public BooleanProperty handShowingProperty() {
    return handShowing;
  }

  @Override
  public Node getView() {
    return this;
  }

  @Override
  public SubScene getSubScene() {
    return scene;
  }

  private void fireRegenerate(
      @Nonnull final String key, @Nonnull final Set<CSG> currentObjectsToCheck) {
    final Thread thread =
        LoggerUtilities.newLoggingThread(
            LOGGER,
            () -> {
              final List<CSG> toAdd = new ArrayList<>();
              final List<CSG> toRemove = new ArrayList<>();

              // For each parameter of each object
              currentObjectsToCheck.forEach(
                  object ->
                      object
                          .getParameters()
                          .forEach(
                              param -> {
                                // If the parameter matches the
                                // input
                                if (param.contentEquals(key) && !toRemove.contains(object)) {
                                  // Regen the csg, remove the
                                  // existing CSG, and add the new
                                  // CSG
                                  final CSG regen = object.regenerate();
                                  toRemove.add(object);
                                  toAdd.add(regen);
                                }
                              }));

              Platform.runLater(
                  () ->
                      toRemove.forEach(item -> meshViewGroup.getChildren().remove(item.getMesh())));
              Platform.runLater(() -> toAdd.forEach(this::addCSG));

              LOGGER.log(Level.INFO, "Saving CSG database");
              CSGDatabase.saveDatabase();
              LOGGER.log(Level.INFO, "Done saving CSG database");
            });

    thread.setName("CAD Regenerate Thread");
    thread.start();
  }
}
