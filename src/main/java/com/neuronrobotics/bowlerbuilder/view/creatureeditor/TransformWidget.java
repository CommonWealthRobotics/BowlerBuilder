/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.creatureeditor;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javax.annotation.Nonnull;

public class TransformWidget implements EngineeringUnitsChangeListener,
    EventHandler<ActionEvent> {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(TransformWidget.class.getSimpleName());
  private final VBox view;
  private final TransformChangeListener onChange;
  private final EngineeringUnitsSliderWidget tilt;
  private final EngineeringUnitsSliderWidget elevation;
  private final EngineeringUnitsSliderWidget azimuth;
  private final EngineeringUnitsSliderWidget xPos;
  private final EngineeringUnitsSliderWidget yPos;
  private final EngineeringUnitsSliderWidget zPos;

  public TransformWidget(@Nonnull final String title, @Nonnull final TransformNR initialState,
      @Nonnull final TransformChangeListener onChange) {
    this.onChange = onChange;

    view = new VBox(5);
    view.setPadding(new Insets(5));

    final GridPane gridPane = new GridPane();
    gridPane.setHgap(10);

    xPos = new EngineeringUnitsSliderWidget(this, initialState.getX(), 100, "mm");
    yPos = new EngineeringUnitsSliderWidget(this, initialState.getY(), 100, "mm");
    zPos = new EngineeringUnitsSliderWidget(this, initialState.getZ(), 100, "mm");

    final RotationNR rot = initialState.getRotation();
    double tilt = 0;
    try {
      tilt = Math.toDegrees(rot.getRotationTilt());
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Could not convert tilt to degrees.\n"
          + Throwables.getStackTraceAsString(e));
    }

    double elevation = 0;
    try {
      elevation = Math.toDegrees(rot.getRotationElevation());
    } catch (final Exception ex) {
      LOGGER.log(Level.WARNING, "Could not convert elevation to degrees.\n"
          + Throwables.getStackTraceAsString(ex));
    }

    double azimuth = 0;
    try {
      azimuth = Math.toDegrees(rot.getRotationAzimuth());
    } catch (final Exception ex) {
      LOGGER.log(Level.WARNING, "Could not convert azimuth to degrees.\n"
          + Throwables.getStackTraceAsString(ex));
    }

    this.tilt = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, tilt, 100, "degrees");
    this.elevation = new EngineeringUnitsSliderWidget(this, -89.99, 89.99, elevation, 100,
        "degrees");
    this.azimuth = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, azimuth, 100, "degrees");

    this.tilt.setAllowResize(false);
    this.elevation.setAllowResize(false);
    this.azimuth.setAllowResize(false);

    final Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font(16));
    view.getChildren().add(titleLabel);

    gridPane.add(getSliderLabel("X"), 0, 0);
    gridPane.add(xPos, 1, 0);

    gridPane.add(getSliderLabel("Y"), 0, 1);
    gridPane.add(yPos, 1, 1);

    gridPane.add(getSliderLabel("Z"), 0, 2);
    gridPane.add(zPos, 1, 2);

    gridPane.add(getSliderLabel("Tilt"), 0, 3);
    gridPane.add(this.tilt, 1, 3);

    gridPane.add(getSliderLabel("Elevation"), 0, 4);
    gridPane.add(this.elevation, 1, 4);

    gridPane.add(getSliderLabel("Azimuth"), 0, 5);
    gridPane.add(this.azimuth, 1, 5);

    view.getChildren().add(gridPane);
  }

  private Label getSliderLabel(@Nonnull final String text) {
    final Label out = new Label(text);
    out.setPadding(new Insets(5));
    GridPane.setHalignment(out, HPos.RIGHT);
    return out;
  }

  private TransformNR getCurrent() {
    return new TransformNR(
        xPos.getValue(),
        yPos.getValue(),
        zPos.getValue(),
        new RotationNR(
            tilt.getValue(),
            azimuth.getValue(),
            elevation.getValue()
        ));
  }

  @Override
  public void onSliderMoving(@Nonnull final EngineeringUnitsSliderWidget source,
      final double newAngleDegrees) {
    onChange.onTransformChanging(getCurrent());
  }

  @Override
  public void onSliderDoneMoving(@Nonnull final EngineeringUnitsSliderWidget source,
      final double newAngleDegrees) {
    handle(null);
  }

  @Override
  public void handle(final ActionEvent event) {
    onChange.onTransformChanging(getCurrent());
    onChange.onTransformFinished(getCurrent());
  }

  public Node getView() {
    return view;
  }

}
