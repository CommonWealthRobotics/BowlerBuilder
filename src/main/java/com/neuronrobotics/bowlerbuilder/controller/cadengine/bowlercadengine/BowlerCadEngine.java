/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine; // NOPMD

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.util.VirtualCameraMobileBaseFactory;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesServiceFactory;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraMobileBase;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.XForm;
import com.neuronrobotics.bowlerbuilder.view.cadengine.element.Axis3D;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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
import javafx.stage.FileChooser;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.io.FileUtils;

@ParametersAreNonnullByDefault
public class BowlerCadEngine extends Pane implements CadEngine {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(BowlerCadEngine.class.getSimpleName());

  private final SubScene scene;
  private final XForm world = new XForm();
  private final PerspectiveCamera camera = new PerspectiveCamera(true);

  private final Group root = new Group();
  private final Group lookGroup = new Group();
  private final Group focusGroup = new Group();
  private final Group meshViewGroup = new Group();
  private final Group ground = new Group();
  private final Group axisGroup = new Group();
  private final Group gridGroup = new Group();
  private final Group hand = new Group();

  private final Map<MeshView, Axis3D> axisMap = new ConcurrentHashMap<>();
  private VirtualCameraDevice virtualCam;
  private VirtualCameraMobileBase flyingCamera;
  private TransformNR defaultCameraView;

  private final CSGManager csgManager;
  private final SelectionManager selectionManager;

  private final BooleanProperty axisShowing;
  private final BooleanProperty handShowing;

  /**
   * CAD Engine from BowlerStudio.
   *
   * @param csgManager {@link CSGManager}
   * @param selectionManagerFactory {@link SelectionManager}
   * @param preferencesServiceFactory {@link PreferencesServiceFactory}
   */
  @Inject
  public BowlerCadEngine(
      final CSGManager csgManager,
      final SelectionManagerFactory selectionManagerFactory,
      final PreferencesServiceFactory preferencesServiceFactory) {
    super();
    this.csgManager = csgManager;

    axisShowing = new SimpleBooleanProperty(true);
    handShowing = new SimpleBooleanProperty(true);

    final PreferencesService preferencesService =
        preferencesServiceFactory.create("BowlerCadEngine");
    preferencesService.load();
    final Boolean shouldAA = preferencesService.get("CAD Engine Antialiasing", true);

    if (shouldAA) {
      scene = new SubScene(root, 1024, 1024, true, SceneAntialiasing.BALANCED);
    } else {
      scene = new SubScene(root, 1024, 1024, true, SceneAntialiasing.DISABLED);
    }

    buildScene();
    buildCamera(); // Initializes virtualCam which we need for selectionManager
    buildAxes();

    this.selectionManager =
        selectionManagerFactory.create(csgManager, focusGroup, virtualCam, this::moveCamera);

    scene.setFill(new LinearGradient(125, 0, 225, 0, false, CycleMethod.NO_CYCLE, (Stop[]) null));
    selectionManager.attachMouseListenersToScene(scene);
    getChildren().add(scene);

    // Clip view so it doesn't overlap with anything
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

  private void buildCamera() {
    buildCameraStatic(camera, hand, scene);

    virtualCam = new VirtualCameraDevice(camera, hand);
    VirtualCameraFactory.setFactory(() -> virtualCam);
    flyingCamera = VirtualCameraMobileBaseFactory.create(virtualCam);
    defaultCameraView = flyingCamera.getFiducialToGlobalTransform();

    moveCamera(new TransformNR(0, 0, 0, new RotationNR(90 - 127, 24, 0)), 0);
  }

  private static void buildCameraStatic(
      final PerspectiveCamera camera, final Group hand, final SubScene scene) {
    camera.setNearClip(.1);
    camera.setFarClip(100000.0);
    scene.setCamera(camera);

    camera.setRotationAxis(Rotate.Z_AXIS);
    camera.setRotate(180);

    final CSG cylinder = new Cylinder(0, 5, 20, 20).toCSG().roty(90).setColor(Color.BLACK);
    hand.getChildren().add(cylinder.getMesh());
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

      final ImageView groundView = new ImageView(groundLocal);
      groundView.getTransforms().addAll(groundMove, downset);
      groundView.setOpacity(0.3);

      final ImageView zrulerImage = new ImageView(ruler);
      zrulerImage.getTransforms().addAll(zRuler, downset);

      final ImageView rulerImage = new ImageView(ruler);
      rulerImage.getTransforms().addAll(xRuler, downset);

      final ImageView yrulerImage = new ImageView(ruler);
      yrulerImage.getTransforms().addAll(yRuler, downset);

      Platform.runLater(
          () -> {
            gridGroup.getChildren().addAll(zrulerImage, rulerImage, yrulerImage, groundView);

            final Affine groundPlacement = new Affine();
            groundPlacement.setTz(-1);
            ground.getTransforms().add(groundPlacement);
            focusGroup.getChildren().add(virtualCam.getCameraFrame());

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
   * Move the camera.
   *
   * @param newPose transform to move by
   * @param seconds seconds to move over
   */
  private void moveCamera(final TransformNR newPose, final double seconds) {
    flyingCamera.DriveArc(newPose, seconds);
  }

  /** Home the camera to its default view. */
  @Override
  public void homeCamera() {
    flyingCamera.setGlobalToFiducialTransform(defaultCameraView);
    virtualCam.setZoomDepth(VirtualCameraDevice.getDefaultZoomDepth());
    flyingCamera.updatePositions();
  }

  @Nonnull
  @Override
  public Map<CSG, MeshView> getCsgMap() {
    return csgManager.getCsgToMeshView();
  }

  /**
   * Select all CSGs from the line in the script.
   *
   * @param script script containing CSG source
   * @param lineNumber line number in script
   */
  @Override
  public void setSelectedCSG(final File script, final int lineNumber) {
    selectionManager.setSelectedCSG(script, lineNumber);
  }

  /**
   * Select a CSG.
   *
   * @param selection CSG to select
   */
  @Override
  public void selectCSG(final CSG selection) {
    selectionManager.selectCSG(selection);
  }

  /**
   * Select all CSGs in the collection.
   *
   * @param selection CSGs to select
   */
  @Override
  public void selectCSGs(final Iterable<? extends CSG> selection) {
    selectionManager.selectCSGs(selection);
  }

  /**
   * Add a CSG to the scene graph.
   *
   * @param csg CSG to add
   */
  @Override
  public void addCSG(final CSG csg) {
    final MeshView mesh = csg.getMesh();
    mesh.setMaterial(new PhongMaterial(csg.getColor()));
    mesh.setDepthTest(DepthTest.ENABLE);
    mesh.setCullFace(CullFace.BACK);

    if (csg.getName() != null && !"".equals(csg.getName()) && csgManager.has(csg.getName())) {
      mesh.setDrawMode(csgManager.getMeshView(csg.getName()).getDrawMode());
    } else {
      mesh.setDrawMode(DrawMode.FILL);
    }

    mesh.setOnMouseClicked(
        mouseEvent -> {
          if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            selectionManager.mouseEvent(mouseEvent, csg);
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

    // TODO: Figure out how to cancel selection on a key press
    mesh.addEventFilter(
        KeyEvent.KEY_PRESSED,
        keyEvent -> {
          LOGGER.info("key event: " + keyEvent.getCode().getName());
          if (KeyCode.ESCAPE.equals(keyEvent.getCode())) {
            LOGGER.info("hit escape");
            selectionManager.cancelSelection();
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

    csgManager.addCSG(csg, mesh);
    LOGGER.log(Level.FINE, "Added CSG with name: " + csg.getName());
  }

  @Override
  public void addAllCSGs(final CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addCSG);
  }

  @Override
  public void addAllCSGs(final Iterable<? extends CSG> csgs) {
    csgs.forEach(this::addCSG);
  }

  @Override
  public void clearMeshes() {
    try {
      FxUtil.runFXAndWait(() -> meshViewGroup.getChildren().clear());
    } catch (InterruptedException ignored) {
    }

    csgManager.getCsgToMeshView().clear();
  }

  @Nonnull
  @Override
  public BooleanProperty axisShowingProperty() {
    return axisShowing;
  }

  @Nonnull
  @Override
  public BooleanProperty handShowingProperty() {
    return handShowing;
  }

  @Nonnull
  @Override
  public Node getView() {
    return this;
  }

  @Nonnull
  @Override
  public SubScene getSubScene() {
    return scene;
  }

  private void fireRegenerate(final String key, final Set<CSG> currentObjectsToCheck) {
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
