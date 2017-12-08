package com.neuronrobotics.bowlerbuilder.controller;

import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Affine;

public class VirtualCameraDevice extends AbstractImageProvider {

  private static final int DEFAULT_ZOOM_DEPTH = -1500;
  private static final Affine offset = TransformFactory.nrToAffine(
      new TransformNR(0, 0, 0, new RotationNR(180, 0, 0))
  );
  private final Group cameraFrame = new Group();
  private PerspectiveCamera camera;
  private double zoomDepth = getDefaultZoomDepth();
  private Affine zoomAffine = new Affine();
  private Group manipulationFrame;

  public VirtualCameraDevice(PerspectiveCamera camera, Group hand) {
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
    return offset;
  }

  @Override
  public void setGlobalPositionListener(Affine affine) {
    super.setGlobalPositionListener(affine);
    manipulationFrame.getTransforms().clear();
    manipulationFrame.getTransforms().add(affine);
  }

  @Override
  protected boolean captureNewImage(BufferedImage imageData) {
    return false;
  }

  @Override
  public void disconnectDeviceImp() {
  }

  @Override
  public boolean connectDeviceImp() {
    return true;
  }

  @Override
  public ArrayList<String> getNamespacesImp() {
    return new ArrayList<>();
  }

  public PerspectiveCamera getCamera() {
    return camera;
  }

  private void setCamera(PerspectiveCamera camera) {
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

  public void setZoomDepth(double zoomDepth) {
    if (zoomDepth > -2) {
      zoomDepth = -2;
    }

    if (zoomDepth < -5000) {
      zoomDepth = -5000;
    }

    this.zoomDepth = zoomDepth;
    zoomAffine.setTz(getZoomDepth());
  }

  public BufferedImage captureNewImage() {
    return null;
  }

}

