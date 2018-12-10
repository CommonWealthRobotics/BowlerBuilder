/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.cadengine.camera;

/*
 * Copyright (c) 2011, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class XForm extends Group {

  public final Translate translate = new Translate();
  public final Translate translate1 = new Translate();
  public final Translate translate2 = new Translate();

  public final Rotate rotX = new Rotate();
  public final Rotate rotY = new Rotate();
  public final Rotate rotZ = new Rotate();

  public final Scale scale = new Scale();

  { // NOPMD
    rotX.setAxis(Rotate.X_AXIS);
    rotY.setAxis(Rotate.Y_AXIS);
    rotZ.setAxis(Rotate.Z_AXIS);
  }

  /** Instantiates a new xform. */
  public XForm() {
    super();
    getTransforms().addAll(translate, rotZ, rotY, rotX, scale);
  }

  /**
   * Instantiates a new xform.
   *
   * @param rotateOrder the rotate order
   */
  public XForm(final RotateOrder rotateOrder) {
    super();
    // choose the order of rotations based on the rotateOrder
    switch (rotateOrder) {
      case XYZ:
        getTransforms().addAll(translate, translate1, rotZ, rotY, rotX, scale, translate2);
        break;
      case XZY:
        getTransforms().addAll(translate, translate1, rotY, rotZ, rotX, scale, translate2);
        break;
      case YXZ:
        getTransforms().addAll(translate, translate1, rotZ, rotX, rotY, scale, translate2);
        break;
      case YZX:
        getTransforms()
            .addAll(translate, translate1, rotX, rotZ, rotY, scale, translate2); // For Camera
        break;
      case ZXY:
        getTransforms().addAll(translate, translate1, rotY, rotX, rotZ, scale, translate2);
        break;
      case ZYX:
        getTransforms().addAll(translate, translate1, rotX, rotY, rotZ, scale, translate2);
        break;
      default:
        break;
    }
  }

  /**
   * Sets the translate.
   *
   * @param newX the x
   * @param newY the y
   * @param newZ the z
   */
  public void setTranslate(final double newX, final double newY, final double newZ) {
    translate.setX(newX);
    translate.setY(newY);
    translate.setZ(newZ);
  }

  /**
   * Sets the translate.
   *
   * @param newX the x
   * @param newY the y
   */
  public void setTranslate(final double newX, final double newY) {
    translate.setX(newX);
    translate.setY(newY);
  }

  /**
   * Sets the tx.
   *
   * @param newX the new tx
   */
  // Use these methods instead:
  public void setTx(final double newX) {
    translate.setX(newX);
  }

  // Cannot override these methods as they are final:
  // public void setTranslateX(double x) { translate.setX(x); }
  // public void setTranslateY(double y) { translate.setY(y); }
  // public void setTranslateZ(double z) { translate.setZ(z); }

  /**
   * Sets the ty.
   *
   * @param newY the new ty
   */
  public void setTy(final double newY) {
    translate.setY(newY);
  }

  /**
   * Sets the tz.
   *
   * @param newZ the new tz
   */
  public void setTz(final double newZ) {
    translate.setZ(newZ);
  }

  /**
   * Sets the rotate.
   *
   * @param newX the x
   * @param newY the y
   * @param newZ the z
   */
  public void setRotate(final double newX, final double newY, final double newZ) {
    rotX.setAngle(newX);
    rotY.setAngle(newY);
    rotZ.setAngle(newZ);
  }

  /**
   * Sets the rotate x.
   *
   * @param newX the new rotate x
   */
  public void setRotateX(final double newX) {
    rotX.setAngle(newX);
  }

  /**
   * Sets the rotate y.
   *
   * @param newY the new rotate y
   */
  public void setRotateY(final double newY) {
    rotY.setAngle(newY);
  }

  /**
   * Sets the rotate z.
   *
   * @param newZ the new rotate z
   */
  public void setRotateZ(final double newZ) {
    rotZ.setAngle(newZ);
  }

  /**
   * Sets the rotX.
   *
   * @param newX the new rotX
   */
  public void setRotX(final double newX) {
    rotX.setAngle(newX);
  }

  /**
   * Sets the rotY.
   *
   * @param newY the new rotY
   */
  public void setRotY(final double newY) {
    rotY.setAngle(newY);
  }

  /**
   * Sets the rotZ.
   *
   * @param newZ the new rotZ
   */
  public void setRotZ(final double newZ) {
    rotZ.setAngle(newZ);
  }

  /**
   * Sets the scale.
   *
   * @param scaleFactor the new scale
   */
  public void setScale(final double scaleFactor) {
    scale.setX(scaleFactor);
    scale.setY(scaleFactor);
    scale.setZ(scaleFactor);
  }

  /**
   * Sets the scale.
   *
   * @param newX the x
   * @param newY the y
   * @param newZ the z
   */
  public void setScale(final double newX, final double newY, final double newZ) {
    scale.setX(newX);
    scale.setY(newY);
    scale.setZ(newZ);
  }

  /**
   * Sets the sx.
   *
   * @param newX the x
   */
  // Use these methods instead:
  public void setSx(final double newX) {
    scale.setX(newX);
  }

  // Cannot override these methods as they are final:
  // public void setScaleX(double x) { scale.setX(x); }
  // public void setScaleY(double y) { scale.setY(y); }
  // public void setScaleZ(double z) { scale.setZ(z); }

  /**
   * Sets the sy.
   *
   * @param newY the new sy
   */
  public void setSy(final double newY) {
    scale.setY(newY);
  }

  /**
   * Sets the sz.
   *
   * @param newZ the z
   */
  public void setSz(final double newZ) {
    scale.setZ(newZ);
  }

  /**
   * Sets the pivot.
   *
   * @param newX the x
   * @param newY the y
   * @param newZ the z
   */
  public void setPivot(final double newX, final double newY, final double newZ) {
    translate1.setX(newX);
    translate1.setY(newY);
    translate1.setZ(newZ);
    translate2.setX(-newX);
    translate2.setY(-newY);
    translate2.setZ(-newZ);
  }

  /** Reset. */
  public void reset() {
    translate.setX(0.0);
    translate.setY(0.0);
    translate.setZ(0.0);
    rotX.setAngle(0.0);
    rotY.setAngle(0.0);
    rotZ.setAngle(0.0);
    scale.setX(1.0);
    scale.setY(1.0);
    scale.setZ(1.0);
    translate1.setX(0.0);
    translate1.setY(0.0);
    translate1.setZ(0.0);
    translate2.setX(0.0);
    translate2.setY(0.0);
    translate2.setZ(0.0);
  }

  /** Reset tsp. */
  public void resetTSP() {
    translate.setX(0.0);
    translate.setY(0.0);
    translate.setZ(0.0);
    scale.setX(1.0);
    scale.setY(1.0);
    scale.setZ(1.0);
    translate1.setX(0.0);
    translate1.setY(0.0);
    translate1.setZ(0.0);
    translate2.setX(0.0);
    translate2.setY(0.0);
    translate2.setZ(0.0);
  }

  public enum RotateOrder {
    XYZ,
    XZY,
    YXZ,
    YZX,
    ZXY,
    ZYX
  }
}
