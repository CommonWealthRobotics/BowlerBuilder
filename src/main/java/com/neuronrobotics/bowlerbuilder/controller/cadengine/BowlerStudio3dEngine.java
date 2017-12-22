package com.neuronrobotics.bowlerbuilder.controller.cadengine; //NOPMD

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.camera.VirtualCameraDevice;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.camera.VirtualCameraMobileBase;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.element.Axis;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.OnEngineeringUnitsChange;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.camera.XForm;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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
import org.apache.commons.io.FileUtils;
import org.reactfx.util.FxTimer;

public class BowlerStudio3dEngine extends Pane implements CadEngine {

  private static final Logger logger = Logger.getLogger(BowlerStudio3dEngine.class.getSimpleName());

  private final Group axisGroup = new Group();
  private final Group gridGroup = new Group();

  private final XForm world = new XForm();
  private final PerspectiveCamera camera = new PerspectiveCamera(true);

  private final Group root = new Group();
  private final Group lookGroup = new Group();
  private final Group focusGroup = new Group();
  private final Group meshViewGroup = new Group();
  private final Group hand = new Group();
  private final Map<String, MeshView> csgNameMap = new WeakHashMap<>();
  private final Map<MeshView, Axis> axisMap = new WeakHashMap<>();
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
  private Map<CSG, MeshView> csgMap = new HashMap<>();
  private CSG selectedCsg;
  @Inject
  private CsgParser csgParser;
  private long lastMouseMovementTime = System.currentTimeMillis();

  private TransformNR previousTarget = new TransformNR();

  private long lastSelectedTime = System.currentTimeMillis();

  private final BooleanProperty axisShowingProperty;
  private final BooleanProperty handShowingProperty;

  public BowlerStudio3dEngine() {
    LoggerUtilities.setupLogger(logger);

    axisShowingProperty = new SimpleBooleanProperty(true);
    handShowingProperty = new SimpleBooleanProperty(true);

    setSubScene(new SubScene(root, 1024, 1024, true, SceneAntialiasing.BALANCED));
    buildScene();
    buildCamera();
    buildAxes();

    scene.setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, (Stop[]) null));
    handleMouse(scene);
    getChildren().add(scene);

    //Clip view so it doesn't overlap with anything
    final Rectangle engineClip = new Rectangle();
    setClip(engineClip);
    layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
      engineClip.setWidth(newValue.getWidth());
      engineClip.setHeight(newValue.getHeight());
    });

    axisShowingProperty.addListener((observableValue, oldVal, newVal) -> {
      if (newVal) {
        showAxis();
      } else {
        hideAxis();
      }
    });

    handShowingProperty.addListener((observableValue, oldVal, newVal) -> {
      if (newVal) {
        showHand();
      } else {
        hideHand();
      }
    });
  }

  /**
   * Build the scene. Setup camera angle and add world to the root.
   */
  private void buildScene() {
    world.ry.setAngle(-90); //point z upwards
    world.ry.setAngle(180); //arm out towards user
    root.getChildren().add(world);
  }

  /**
   * Build the camera. Setup the pointer, clips, rotation, and position.
   */
  private void buildCamera() {
    camera.setNearClip(.1);
    camera.setFarClip(100000.0);
    scene.setCamera(camera);

    camera.setRotationAxis(Rotate.Z_AXIS);
    camera.setRotate(180);

    CSG cylinder = new Cylinder(
        0, // Radius at the top
        5, // Radius at the bottom
        20, // Height
        20 // resolution
    ).toCSG().roty(90).setColor(Color.BLACK);
    hand.getChildren().add(cylinder.getMesh());
    virtualCam = new VirtualCameraDevice(camera, hand);
    VirtualCameraFactory.setFactory(() -> virtualCam);

    try {
      flyingCamera = new VirtualCameraMobileBase(
          "<root>\n"
              + "<mobilebase>\n"
              + "<driveType>none</driveType>\n"
              + "<name>FlyingCamera</name>\n"
              + "<appendage>\n"
              + "<name>BoomArm</name>\n"
              + "<link>\n"
              + "\t<name>boom</name>\n"
              + "\t<deviceName>" + virtualCam.hashCode() + "</deviceName>\n"
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
    } catch (Exception e) {
      logger.log(Level.SEVERE,
          "Could not load VirtualCameraMobileBase.\n" + Throwables.getStackTraceAsString(e));
    }

    moveCamera(
        new TransformNR(0, 0, 0, new RotationNR(90 - 127, 24, 0)),
        0);

    defaultCameraView = flyingCamera.getFiducialToGlobalTransform();
  }

  /**
   * Builds the axes.
   */
  private void buildAxes() {
    Thread buildThread = LoggerUtilities.newLoggingThread(logger, () -> {
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
          axisGroup.getChildren().addAll(focusGroup, meshViewGroup);
          world.getChildren().addAll(lookGroup, axisGroup);
        });
      } catch (Exception e) {
        logger.log(Level.SEVERE,
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
  private final void showAxis() {
    Platform.runLater(() -> axisGroup.getChildren().add(gridGroup));
    axisMap.forEach((mesh, axis) -> axis.show());
  }

  /**
   * Hide the axes.
   */
  private final void hideAxis() {
    Platform.runLater(() -> axisGroup.getChildren().remove(gridGroup));
    axisMap.forEach((mesh, axis) -> axis.hide());
  }

  private final void showHand() {
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
            new RotationNR(
                mouseDeltaY * modifierFactor * modifier * 2.0,
                mouseDeltaX * modifierFactor * modifier * 2.0,
                0
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
  private void moveCamera(TransformNR newPose, double seconds) {
    flyingCamera.DriveArc(newPose, seconds);
  }

  /**
   * Home the camera to its default view.
   */
  public void homeCamera() {
    flyingCamera.setGlobalToFiducialTransform(defaultCameraView);
    virtualCam.setZoomDepth(VirtualCameraDevice.getDefaultZoomDepth());
    flyingCamera.updatePositions();
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

  private void setSubScene(SubScene scene) {
    this.scene = scene;
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

  /**
   * Select all CSGs from the line in the script.
   *
   * @param script script containing CSG source
   * @param lineNumber line number in script
   */
  public void setSelectedCsg(File script, int lineNumber) {
    Collection<CSG> csgs = csgParser.parseCsgFromSource(script, lineNumber, csgMap);

    lastSelectedTime = System.currentTimeMillis();

    if (csgs.size() == 1) {
      selectCSG(csgs.iterator().next(), csgMap);
    } else {
      selectCSGs(csgs, csgMap);
    }

    resetMouseTime();
  }

  /**
   * Select all CSGs in the collection.
   *
   * @param selection CSGs to select
   * @param csgMap map containing CSGs MeshViews
   */
  private void selectCSGs(Collection<CSG> selection, Map<CSG, MeshView> csgMap) {
    selection.forEach(csg -> {
      MeshView meshView = csgMap.get(csg);
      if (meshView != null) {
        FxTimer.runLater(Duration.ofMillis(20), () ->
            meshView.setMaterial(new PhongMaterial(Color.GOLD)));
      }
    });
  }

  /**
   * Select a CSG and pan the camera to that CSG.
   *
   * @param selection CSG to select
   * @param csgMap map containing CSGs MeshViews
   */
  private void selectCSG(CSG selection, Map<CSG, MeshView> csgMap) {
    if (selection.equals(selectedCsg)) {
      return;
    }

    csgMap.keySet().forEach(key -> Platform.runLater(() ->
        csgMap.get(key).setMaterial(new PhongMaterial(key.getColor()))));

    lastSelectedTime = System.currentTimeMillis();
    selectedCsg = selection;

    FxTimer.runLater(Duration.ofMillis(20), () ->
        getCsgMap().get(selectedCsg).setMaterial(new PhongMaterial(Color.GOLD)));

    double xCenter = selectedCsg.getCenterX();
    double yCenter = selectedCsg.getCenterY();
    double zCenter = selectedCsg.getCenterZ();

    TransformNR poseToMove = new TransformNR();

    if (selectedCsg.getMaxX() < 1 || selectedCsg.getMinX() > -1) {
      poseToMove.translateX(xCenter);
    }

    if (selectedCsg.getMaxY() < 1 || selectedCsg.getMinY() > -1) {
      poseToMove.translateY(yCenter);
    }

    if (selectedCsg.getMaxZ() < 1 || selectedCsg.getMinZ() > -1) {
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
  }

  /**
   * Add a CSG to the scene graph.
   *
   * @param csg CSG to add
   */
  @Override
  public void addCSG(CSG csg) {
    MeshView mesh = csg.getMesh();
    mesh.setMaterial(new PhongMaterial(Color.RED));
    mesh.setDepthTest(DepthTest.ENABLE);
    mesh.setCullFace(CullFace.BACK);

    if (csg.getName() != null
        && !"".equals(csg.getName())
        && csgNameMap.containsKey(csg.getName())) {
      mesh.setDrawMode(csgNameMap.get(csg.getName()).getDrawMode());
    } else {
      mesh.setDrawMode(DrawMode.FILL);
    }

    mesh.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
        selectCSG(csg, csgMap);
      } else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
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

        Set<String> params = csg.getParameters();
        if (params != null) {
          Menu parameters = new Menu("Parameters");
          params.forEach(key -> {
            //Regenerate all objects if their parameters have changed
            Runnable regenerateObjects = () -> {
              //Get the set of objects to check for regeneration after the initial regeneration
              //cycle
              Set<CSG> objects = getCsgMap().keySet();

              //Hide the menu because the parameter is done being changed
              menu.hide();

              fireRegenerate(key, objects);
              resetMouseTime();
            };

            Parameter param = CSGDatabase.get(key);
            csg.setParameterIfNull(key);

            if (param instanceof LengthParameter) {
              LengthParameter lp = (LengthParameter) param;

              EngineeringUnitsSliderWidget widget = new EngineeringUnitsSliderWidget(
                  new OnEngineeringUnitsChange() {
                    @Override
                    public void onSliderMoving(
                        EngineeringUnitsSliderWidget sliderWidget,
                        double newAngleDegrees) {
                      try {
                        csg.setParameterNewValue(key, newAngleDegrees);
                      } catch (Exception e) {
                        logger.log(Level.SEVERE, //NOPMD
                            "Could not set new parameter value.\n"
                                + Throwables.getStackTraceAsString(e));
                      }
                    }

                    @Override
                    public void onSliderDoneMoving(
                        EngineeringUnitsSliderWidget sliderWidget,
                        double newAngleDegrees) {
                      regenerateObjects.run();
                    }
                  },
                  Double.parseDouble(lp.getOptions().get(1)),
                  Double.parseDouble(lp.getOptions().get(0)),
                  lp.getMM(),
                  400,
                  key);

              CustomMenuItem customMenuItem = new CustomMenuItem(widget);
              customMenuItem.setHideOnClick(false); //Regen will hide the menu
              parameters.getItems().add(customMenuItem);
            } else {
              if (param != null) {
                Menu paramTypes = new Menu(param.getName() + " " + param.getStrValue());

                param.getOptions().forEach(option -> {
                  MenuItem customMenuItem = new MenuItem(option);
                  customMenuItem.setOnAction(event -> {
                    param.setStrValue(option);
                    CSGDatabase.get(param.getName()).setStrValue(option);
                    CSGDatabase.getParamListeners(param.getName())
                        .forEach(l -> l.parameterChanged(param.getName(), param));
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
              logger.log(Level.SEVERE,
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

    meshViewGroup.getChildren().add(mesh);
    csgMap.put(csg, mesh);
    csgNameMap.put(csg.getName(), mesh);
    logger.log(Level.FINE, "Added CSG with name: " + csg.getName());
  }

  @Override
  public void addAllCSGs(CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addCSG);
  }

  @Override
  public void addAllCSGs(Collection<CSG> csgs) {
    csgs.forEach(this::addCSG);
  }

  @Override
  public void clearMeshes() {
    meshViewGroup.getChildren().clear();
  }

  @Override
  public BooleanProperty axisShowingProperty() {
    return axisShowingProperty;
  }

  @Override
  public BooleanProperty handShowingProperty() {
    return handShowingProperty;
  }

  @Override
  public Node getView() {
    return this;
  }

  @Override
  public SubScene getSubScene() {
    return scene;
  }

  private void fireRegenerate(String key, Set<CSG> currentObjectsToCheck) {
    Thread thread = LoggerUtilities.newLoggingThread(logger, () -> {
      List<CSG> toAdd = new ArrayList<>();
      List<CSG> toRemove = new ArrayList<>();

      //For each parameter of each object
      currentObjectsToCheck.forEach(object -> object.getParameters().forEach(param -> {
        //If the parameter matches the input
        if (param.contentEquals(key) && !toRemove.contains(object)) {
          //Regen the csg, remove the existing CSG, and add the new CSG
          CSG regen = object.regenerate();
          toRemove.add(object);
          toAdd.add(regen);
        }
      }));

      Platform.runLater(() ->
          toRemove.forEach(item -> meshViewGroup.getChildren().remove(item.getMesh())));
      Platform.runLater(() ->
          toAdd.forEach(this::addCSG));

      logger.log(Level.INFO, "Saving CSG database");
      CSGDatabase.saveDatabase();
      logger.log(Level.INFO, "Done saving CSG database");
    });

    thread.setDaemon(true);
    thread.setName("CAD Regenerate Thread");
    thread.start();
  }

}
