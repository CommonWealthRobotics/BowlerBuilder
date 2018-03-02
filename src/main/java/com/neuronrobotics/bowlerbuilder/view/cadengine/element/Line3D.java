package com.neuronrobotics.bowlerbuilder.view.cadengine.element;

import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Affine;

public class Line3D extends Cylinder {

  private double endZ;
  private double startZ;

  public Line3D(final Vertex start, final Vertex end) {
    this(start.pos, end.pos);
  }

  public Line3D(final double[] start, final double[] end) { //NOPMD
    this(start[0], start[1], start[2], end[0], end[1], end[2]);
  }

  public Line3D(final Vector3d start, final Vector3d end) {
    this(start.x, start.y, start.z, end.x, end.y, end.z);
  }

  public Line3D(final double endX, final double endY, final double endZ) {
    this(0, 0, 0, endX, endY, endZ);
  }

  public Line3D(final double startX, final double startY, final double startZ,
      final double endX, final double endY, final double endZ) {
    super(
        0.1,
        Math.sqrt(Math.pow(endX - startX, 2)
            + Math.pow(endY - startY, 2)
            + Math.pow(endZ - startZ, 2))
    );

    final double xDiff = endX - startX;
    final double yDiff = endY - startY;
    final double zDiff = endZ - startZ;
    final double lineLen = getHeight();

    final double xyProjection = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));

    final Affine xyRot = new Affine();
    final double rotY = Math.toDegrees(Math.atan2(xyProjection, zDiff));
    xyRot.appendRotation(-90 - rotY, 0, 0, 0, 0, 1, 0);

    final Affine orent = new Affine();
    orent.appendRotation(90, 0, 0, 0, 0, 0, 1);
    orent.setTx(lineLen / 2);

    final Affine zRot = new Affine();
    final double rotZ = Math.toDegrees(Math.atan2(xDiff, yDiff));
    zRot.appendRotation(-90 - rotZ, 0, 0, 0, 0, 0, 1);
    final Affine zTrans = new Affine();
    zTrans.setTx(startX);
    zTrans.setTy(startY);
    zTrans.setTz(startZ);

    getTransforms().add(zTrans);
    getTransforms().add(zRot);
    getTransforms().add(xyRot);

    getTransforms().add(orent);

    final Affine orent2 = new Affine();
    getTransforms().add(orent2);
  }

  public double getEndZ() {
    return endZ;
  }

  public void setEndZ(final double endZ) {
    this.endZ = endZ;
  }

  public double getStartZ() {
    return startZ;
  }

  public void setStartZ(final double startZ) {
    this.startZ = startZ;
  }

  public void setStrokeWidth(final double radius) {
    setRadius(radius / 2);
  }

  public void setStroke(final Color color) {
    Platform.runLater(() -> setMaterial(new PhongMaterial(color)));
  }

}
