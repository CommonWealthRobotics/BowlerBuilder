package com.neuronrobotics.bowlerbuilder.controller.cadengine;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

    /*
     *      Axis.java 1.0 98/11/25
     *
     * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
     *
     * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
     * modify and redistribute this software in source and binary code form,
     * provided that i) this copyright notice and license appear on all copies of
     * the software; and ii) Licensee does not utilize the software in a manner
     * which is disparaging to Sun.
     *
     * This software is provided "AS IS," without a warranty of any kind. ALL
     * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
     * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
     * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
     * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
     * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
     * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
     * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
     * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
     * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
     * POSSIBILITY OF SUCH DAMAGES.
     *
     * This software is not designed or intended for use in on-line control of
     * aircraft, air traffic, aircraft navigation or aircraft communications; or in
     * the design, construction, operation or maintenance of any nuclear
     * facility. Licensee represents and warrants that it will not use or
     * redistribute the Software for such purposes.
     */

public class Axis extends Group {

  private Line3D xAxis;
  private Line3D yAxis;
  private Line3D zAxis;
  private Label xText;
  private Label yText;
  private Label zText;


  public Axis() {
    this(50);
  }

  public Axis(int i) {
    Affine xp = new Affine();
    xp.setTx(i / 2);
    xText = new Label("+X");
    xText.getTransforms().add(xp);

    Affine yp = new Affine();
    yp.setTy(i / 2);
    yText = new Label("+Y");
    yText.getTransforms().add(yp);

    Affine zTextAffine = new Affine();
    zTextAffine.setTz(i / 2);
    zTextAffine.setTx(i / 2);
    zTextAffine.appendRotation(-90, 0, 0, 0, 1, 0, 0);
    zTextAffine.appendRotation(180, 0, 0, 0, 0, 0, 1);
    zText = new Label("+Z");
    zText.getTransforms().add(zTextAffine);

    double strokeWidth = 0.1;
    double inset = 0;
    xAxis = new Line3D(0, inset, 0, i, inset, 0);
    yAxis = new Line3D(inset, 0, 0, inset, i, 0);
    zAxis = new Line3D(inset, 0, 0, inset, 0, i);

    xAxis.setStrokeWidth(strokeWidth);
    xAxis.setStroke(Color.RED);

    yAxis.setStrokeWidth(strokeWidth);
    yAxis.setStroke(Color.GREEN);

    zAxis.setStrokeWidth(strokeWidth);
    zAxis.setStroke(Color.BLUE);

    show();
  }

  public void show() {
    Platform.runLater(() -> getChildren().addAll(xAxis, yAxis, zAxis, xText, yText, zText));
  }

  public void hide() {
    Platform.runLater(() -> getChildren().removeAll(xAxis, yAxis, zAxis, xText, yText, zText));
  }

}
