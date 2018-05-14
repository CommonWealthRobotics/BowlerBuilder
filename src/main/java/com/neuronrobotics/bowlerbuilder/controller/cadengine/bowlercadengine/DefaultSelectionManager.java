/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine;

import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javax.annotation.ParametersAreNonnullByDefault;
import org.reactfx.util.FxTimer;

@ParametersAreNonnullByDefault
public class DefaultSelectionManager implements SelectionManager {

  private final CSGManager csgManager;
  private final Group focusGroup;
  private final VirtualCameraDevice virtualCam;
  private final BiConsumer<TransformNR, Double> moveCamera;

  private double mousePosX;
  private double mousePosY;
  private double mouseOldX;
  private double mouseOldY;
  private double mouseDeltaX;
  private double mouseDeltaY;
  private TransformNR previousTarget = new TransformNR();
  private CSG selectedCSG;

  /**
   * Manages {@link CSG} selections (from the GUI or programmatically).
   *
   * @param csgManager {@link CSGManager} to pull the CSG map from
   * @param focusGroup focus group
   * @param virtualCam virtual camera
   * @param moveCamera {@link BiConsumer} to move the camera around for a new selection
   */
  public DefaultSelectionManager(
      final CSGManager csgManager,
      final Group focusGroup,
      final VirtualCameraDevice virtualCam,
      final BiConsumer<TransformNR, Double> moveCamera) {
    this.csgManager = csgManager;
    this.focusGroup = focusGroup;
    this.virtualCam = virtualCam;
    this.moveCamera = moveCamera;
  }

  /**
   * Select all CSGs from the line in the script.
   *
   * @param script script containing CSG source
   * @param lineNumber line number in script
   */
  @Override
  public void setSelectedCSG(final File script, final int lineNumber) {
    Platform.runLater(
        () -> {
          final Collection<CSG> csgs =
              csgManager
                  .getCsgParser()
                  .parseCsgFromSource(script.getName(), lineNumber, csgManager.getCsgMap());

          if (csgs.size() == 1) {
            selectCSG(csgs.iterator().next());
          } else {
            selectCSGs(csgs);
          }
        });
  }

  /**
   * Select a CSG and pan the camera to that CSG.
   *
   * @param selection CSG to select
   */
  @Override
  public void selectCSG(final CSG selection) {
    if (selection.equals(selectedCSG)) {
      return;
    }

    csgManager
        .getCsgMap()
        .keySet()
        .forEach(
            key ->
                Platform.runLater(
                    () ->
                        csgManager
                            .getCsgMap()
                            .get(key)
                            .setMaterial(new PhongMaterial(key.getColor()))));

    selectedCSG = selection;

    FxTimer.runLater(
        Duration.ofMillis(20),
        () -> csgManager.getCsgMap().get(selectedCSG).setMaterial(new PhongMaterial(Color.GOLD)));

    final double xCenter = selectedCSG.getCenterX();
    final double yCenter = selectedCSG.getCenterY();
    final double zCenter = selectedCSG.getCenterZ();

    final TransformNR poseToMove = new TransformNR();

    if (selectedCSG.getMaxX() < 1 || selectedCSG.getMinX() > -1) {
      poseToMove.translateX(xCenter);
    }

    if (selectedCSG.getMaxY() < 1 || selectedCSG.getMinY() > -1) {
      poseToMove.translateY(yCenter);
    }

    if (selectedCSG.getMaxZ() < 1 || selectedCSG.getMinZ() > -1) {
      poseToMove.translateZ(zCenter);
    }

    final Affine centering = TransformFactory.nrToAffine(poseToMove);
    // this section keeps the camera oriented the same way to avoid whipping around
    final TransformNR rotationOnlyComponentOfManipulator =
        TransformFactory.affineToNr(selectedCSG.getManipulator());
    rotationOnlyComponentOfManipulator.setX(0);
    rotationOnlyComponentOfManipulator.setY(0);
    rotationOnlyComponentOfManipulator.setZ(0);
    final TransformNR reverseRotation = rotationOnlyComponentOfManipulator.inverse();

    final TransformNR startSelectNr = previousTarget.copy();
    final TransformNR targetNR;

    if (checkManipulator()) {
      targetNR = TransformFactory.affineToNr(selectedCSG.getManipulator());
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
            focusGroup.getTransforms().add(selectedCSG.getManipulator());
            focusGroup.getTransforms().add(correction);
          } else {
            focusGroup.getTransforms().add(centering);
          }
          focusInterpolate(startSelectNr, targetNR, 0, 30, interpolator);
        });
  }
  /**
   * Select all CSGs in the collection.
   *
   * @param selection CSGs to select
   */
  @Override
  public void selectCSGs(final Iterable<? extends CSG> selection) {
    selection.forEach(
        csg -> {
          final MeshView meshView = csgManager.getCsgMap().get(csg);
          if (meshView != null) {
            FxTimer.runLater(
                Duration.ofMillis(20), () -> meshView.setMaterial(new PhongMaterial(Color.GOLD)));
          }
        });
  }

  /** De-select the selection. */
  @Override
  public void cancelSelection() {
    for (final CSG key : csgManager.getCsgMap().keySet()) {
      Platform.runLater(
          () ->
              csgManager
                  .getCsgMap()
                  .get(key)
                  .setMaterial(new PhongMaterial(key.getColor()))); // NOPMD
    }

    selectedCSG = null; // NOPMD
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
  }

  /**
   * Handle a mouse event from the 3D window.
   *
   * @param mouseEvent JavaFX-generated mouse event
   * @param csg CSG the event was generated from
   */
  @Override
  public void mouseEvent(final MouseEvent mouseEvent, final CSG csg) {
    selectCSG(csg);

    mouseOldX = mousePosX;
    mouseOldY = mousePosY;
    mousePosX = mouseEvent.getSceneX();
    mousePosY = mouseEvent.getSceneY();
  }

  /**
   * Attach mouse listeners to the scene. Side-effects the scene.
   *
   * @param scene the scene
   */
  @Override
  public void attachMouseListenersToScene(final SubScene scene) {
    scene.setOnMousePressed(
        mouseEvent -> {
          mouseOldX = mousePosX;
          mouseOldY = mousePosY;
          mousePosX = mouseEvent.getSceneX();
          mousePosY = mouseEvent.getSceneY();
        });

    scene.setOnMouseDragged(
        mouseEvent -> {
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
              moveCamera.accept(trans, 0.0);
            }
          } else if (mouseEvent.isSecondaryButtonDown()) {
            final double depth = -100 / virtualCam.getZoomDepth();
            moveCamera.accept(
                new TransformNR(
                    mouseDeltaX * modifierFactor * modifier * 1 / depth,
                    mouseDeltaY * modifierFactor * modifier * 1 / depth,
                    0,
                    new RotationNR()),
                0.0);
          }
        });

    scene.addEventHandler(
        ScrollEvent.ANY,
        event -> {
          if (ScrollEvent.SCROLL == event.getEventType()) {
            final double zoomFactor = -(event.getDeltaY()) * virtualCam.getZoomDepth() / 500;
            virtualCam.setZoomDepth(virtualCam.getZoomDepth() + zoomFactor);
          }
          event.consume();
        });
  }

  private void focusInterpolate(
      final TransformNR start,
      final TransformNR target,
      final int depth,
      final int targetDepth,
      final Affine interpolator) {

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

  private boolean checkManipulator() {
    return Math.abs(selectedCSG.getManipulator().getTx()) > 0.1
        || Math.abs(selectedCSG.getManipulator().getTy()) > 0.1
        || Math.abs(selectedCSG.getManipulator().getTz()) > 0.1;
  }
}
