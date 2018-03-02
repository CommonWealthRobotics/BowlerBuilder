package com.neuronrobotics.bowlerbuilder.view.cadengine;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

public class EngineeringUnitsSliderWidget extends GridPane implements ChangeListener<Number> {

  private TextField setpointValue;
  private Slider setpoint;
  private EngineeringUnitsChangeListener listener;
  private boolean intCast;
  private boolean allowResize = true;

  public EngineeringUnitsSliderWidget(EngineeringUnitsChangeListener listener, double min,
      double max, double current, double width, String units, boolean intCast) {
    this(listener, min, max, current, width, units);
    this.intCast = intCast;
  }

  public EngineeringUnitsSliderWidget(
      EngineeringUnitsChangeListener listener,
      double current,
      double width,
      String units) {
    this(listener, current / 2, current * 2, current, width, units);
  }

  public EngineeringUnitsSliderWidget(
      EngineeringUnitsChangeListener listener,
      double min,
      double max,
      double current,
      double width,
      String units) {
    this.listener = listener;
    setpoint = new Slider();

    double fixedMin = min;
    double fixedMax = max;

    if (fixedMin > fixedMax) {
      double minStart = fixedMin;
      fixedMin = fixedMax;
      fixedMax = minStart;
    }

    double range = Math.abs(fixedMax - fixedMin);
    if (range < 1) {
      fixedMin = -100;
      fixedMax = 100;
      range = 200;
    }

    setpoint.setMin(fixedMin);
    setpoint.setMax(fixedMax);
    setpoint.setValue(current);
    setpoint.setShowTickLabels(true);
    setpoint.setShowTickMarks(true);

    setpoint.setMajorTickUnit(range);
    setpoint.setMinorTickCount(5);

    setpointValue = new TextField(getFormatted(current));
    setpointValue.setOnAction(event -> Platform.runLater(() -> {
      double val = Double.parseDouble(setpointValue.getText());
      setValue(val);
      getListener().onSliderMoving(this, val);
      getListener().onSliderDoneMoving(this, val);
    }));

    setpoint.setMaxWidth(width);
    setpoint.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
      double val = Double.parseDouble(setpointValue.getText());
      if (!newValue) {
        getListener().onSliderDoneMoving(this, val);
      }
    });
    setpoint.valueProperty().addListener(this);

    setAlignment(Pos.CENTER_LEFT);
    getColumnConstraints().add(new ColumnConstraints(width + 20));
    getColumnConstraints().add(new ColumnConstraints(100));

    String unitsString = "(" + units + ")";
    getColumnConstraints().add(new ColumnConstraints(unitsString.length() * 7));

    add(setpoint, 0, 0);
    add(setpointValue, 1, 0);
    Text unitText = new Text(unitsString);
    HBox unitTextWrapper = new HBox(unitText); //Wrapper so we get padding
    unitTextWrapper.setPadding(new Insets(0, 0, 0, 5));
    unitTextWrapper.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(unitTextWrapper, Priority.NEVER);
    add(unitTextWrapper, 2, 0);
  }

  public void setUpperBound(double newBound) {
    setpoint.setMax(newBound);
  }

  public void setLowerBound(double newBound) {
    setpoint.setMin(newBound);
  }

  @Override
  public void changed(ObservableValue<? extends Number> observable,
      Number oldValue,
      Number newValue) {
    updateValue();
  }

  private void updateValue() {
    Platform.runLater(() -> {
      setpointValue.setText(getFormatted(setpoint.getValue()));
      getListener().onSliderMoving(this, setpoint.getValue());
    });
  }

  /**
   * Set the value of this slider.
   *
   * @param value new value
   */
  public void setValue(double value) {
    Platform.runLater(() -> {
      setpoint.valueProperty().removeListener(this);
      double val = value;
      if (val > setpoint.getMax()) {
        if (isAllowResize()) {
          setpoint.setMax(val);
        } else {
          val = setpoint.getMax();
        }
      }

      if (val < setpoint.getMin()) {
        if (isAllowResize()) {
          setpoint.setMin(val);
        } else {
          val = setpoint.getMin();
        }
      }

      double range = Math.abs(setpoint.getMax() - setpoint.getMin());
      setpoint.setMajorTickUnit(range);
      setpoint.setValue(val);
      setpointValue.setText(getFormatted(setpoint.getValue()));
      setpoint.valueProperty().addListener(this);
    });
  }

  public double getValue() {
    return setpoint.getValue();
  }

  /**
   * Format a value according to this slider's formatting rules.
   *
   * @param value the value to format
   * @return formatted version
   */
  public String getFormatted(double value) {
    if (intCast) {
      return String.valueOf((int) value);
    }

    return String.format("%8.2f", (double) value);
  }

  /**
   * Get the listener for when the value of this slider changes.
   *
   * @return change listener
   */
  public EngineeringUnitsChangeListener getListener() {
    if (listener == null) {
      return new EngineeringUnitsChangeListener() {
        @Override
        public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
          //Default is nothing
        }

        @Override
        public void onSliderDoneMoving(EngineeringUnitsSliderWidget source,
            double newAngleDegrees) {
          //Default is nothing
        }
      };
    }

    return listener;
  }

  public void setListener(EngineeringUnitsChangeListener listener) {
    this.listener = listener;
  }

  public boolean isAllowResize() {
    return allowResize;
  }

  public void setAllowResize(boolean allowResize) {
    this.allowResize = allowResize;
  }

}
