package com.neuronrobotics.bowlerbuilder.view.robotmanager;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TransformWidget implements EngineeringUnitsChangeListener,
    EventHandler<ActionEvent> {

  private final VBox view;
  private final GridPane gridPane;
  private TransformChangeListener onChange;
  private EngineeringUnitsSliderWidget tilt;
  private EngineeringUnitsSliderWidget elevation;
  private EngineeringUnitsSliderWidget azimuth;
  private EngineeringUnitsSliderWidget tx;
  private EngineeringUnitsSliderWidget ty;
  private EngineeringUnitsSliderWidget tz;
  private TransformNR initialState;

  public TransformWidget(String title, TransformNR is, TransformChangeListener onChange) {
    this.initialState = is;
    this.onChange = onChange;

    view = new VBox(5);
    view.setPadding(new Insets(5));

    gridPane = new GridPane();
    gridPane.setHgap(10);

    tx = new EngineeringUnitsSliderWidget(this, initialState.getX(), 100, "mm");
    ty = new EngineeringUnitsSliderWidget(this, initialState.getY(), 100, "mm");
    tz = new EngineeringUnitsSliderWidget(this, initialState.getZ(), 100, "mm");

    RotationNR rot = initialState.getRotation();
    double t = 0;
    try {
      t = Math.toDegrees(rot.getRotationTilt());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    double e = 0;
    try {
      e = Math.toDegrees(rot.getRotationElevation());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    double a = 0;
    try {
      a = Math.toDegrees(rot.getRotationAzimuth());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    tilt = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, t, 100, "degrees");
    elevation = new EngineeringUnitsSliderWidget(this, -89.99, 89.99, e, 100, "degrees");
    azimuth = new EngineeringUnitsSliderWidget(this, -179.99, 179.99, a, 100, "degrees");

    tilt.setAllowResize(false);
    elevation.setAllowResize(false);
    azimuth.setAllowResize(false);

    Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font(16));
    view.getChildren().add(titleLabel);

    gridPane.add(getSliderLabel("X"), 0, 0);
    gridPane.add(tx, 1, 0);

    gridPane.add(getSliderLabel("Y"), 0, 1);
    gridPane.add(ty, 1, 1);

    gridPane.add(getSliderLabel("Z"), 0, 2);
    gridPane.add(tz, 1, 2);

    gridPane.add(getSliderLabel("Tilt"), 0, 3);
    gridPane.add(tilt, 1, 3);

    gridPane.add(getSliderLabel("Elevation"), 0, 4);
    gridPane.add(elevation, 1, 4);

    gridPane.add(getSliderLabel("Azimuth"), 0, 5);
    gridPane.add(azimuth, 1, 5);

    view.getChildren().add(gridPane);
  }

  private Label getSliderLabel(String text) {
    Label out = new Label(text);
    out.setPadding(new Insets(5));
    GridPane.setHalignment(out, HPos.RIGHT);
    return out;
  }

  private TransformNR getCurrent() {
    return new TransformNR(
        tx.getValue(),
        ty.getValue(),
        tz.getValue(),
        new RotationNR(
            tilt.getValue(),
            azimuth.getValue(),
            elevation.getValue()
        ));
  }

  @Override
  public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    onChange.onTransformChanging(getCurrent());
  }

  @Override
  public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    handle(null);
  }

  @Override
  public void handle(ActionEvent event) {
    onChange.onTransformChanging(getCurrent());
    onChange.onTransformFinished(getCurrent());
  }

  public void updatePose(TransformNR p) {
    tx.setValue(p.getX());
    ty.setValue(p.getY());
    tz.setValue(p.getZ());

    RotationNR rot = p.getRotation();
    double t = 0;
    try {
      t = Math.toDegrees(rot.getRotationTilt());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    double e = 0;
    try {
      e = Math.toDegrees(rot.getRotationElevation());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    double a = 0;
    try {
      a = Math.toDegrees(rot.getRotationAzimuth());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    tilt.setValue(t);
    elevation.setValue(e);
    azimuth.setValue(a);
  }

  public Node getView() {
    return view;
  }

}
