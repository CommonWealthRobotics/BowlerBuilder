/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera;

import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.transform.Affine;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class VirtualCameraDevice extends AbstractImageProvider {

  private static final int DEFAULT_ZOOM_DEPTH = -1500;
  private static final Affine OFFSET =
      TransformFactory.nrToAffine(new TransformNR(0, 0, 0, new RotationNR(180, 0, 0)));
  private final Group cameraFrame = new Group();
  private Camera camera;
  private double zoomDepth = getDefaultZoomDepth();
  private final Affine zoomAffine = new Affine();
  private final Group manipulationFrame;

  public VirtualCameraDevice(final Camera camera, final Group hand) {
    super();
    setCamera(camera);
    setScriptingName("virtualCameraDevice");

    manipulationFrame = new Group();
    camera.getTransforms().add(zoomAffine);

    cameraFrame.getTransforms().add(getOffset());
    manipulationFrame.getChildren().addAll(camera, hand);
    cameraFrame.getChildren().add(manipulationFrame);

    setZoomDepth(DEFAULT_ZOOM_DEPTH);
  }

  public static int getDefaultZoomDepth() {
    return DEFAULT_ZOOM_DEPTH;
  }

  public static Affine getOffset() {
    return OFFSET;
  }

  @Override
  public void setGlobalPositionListener(final Affine affine) {
    super.setGlobalPositionListener(affine);
    manipulationFrame.getTransforms().clear();
    manipulationFrame.getTransforms().add(affine);
  }

  @Override
  protected boolean captureNewImage(final BufferedImage imageData) {
    return false;
  }

  @Override
  public BufferedImage captureNewImage() {
    return null;
  }

  /** Nothing to disconnect. */
  @Override
  public void disconnectDeviceImp() {
    // Not used
  }

  /**
   * Nothing to connect.
   *
   * @return true
   */
  @Override
  public boolean connectDeviceImp() {
    return true;
  }

  @Override
  public ArrayList<String> getNamespacesImp() {
    return new ArrayList<>();
  }

  public Camera getCamera() {
    return camera;
  }

  private void setCamera(final Camera camera) {
    this.camera = camera;
  }

  public Group getCameraGroup() {
    return getCameraFrame();
  }

  public Group getCameraFrame() {
    return cameraFrame;
  }

  public double getZoomDepth() {
    return zoomDepth;
  }

  /**
   * Set the zoom depth to zoom the camera.
   *
   * @param zoomDepth new zoom depth
   */
  public void setZoomDepth(final double zoomDepth) {
    this.zoomDepth = zoomDepth;

    if (this.zoomDepth > -2) {
      this.zoomDepth = -2;
    }

    if (this.zoomDepth < -5000) {
      this.zoomDepth = -5000;
    }

    zoomAffine.setTz(this.zoomDepth);
  }
}
