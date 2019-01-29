/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine;

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
import javax.annotation.Nonnull;

// From BowlerStudio
public class EngineeringUnitsSliderWidget extends GridPane implements ChangeListener<Number> {

  private final TextField setpointValue;
  private final Slider setpoint;
  private EngineeringUnitsChangeListener listener;
  private boolean intCast;
  private boolean allowResize = true;

  // CHECKSTYLE:OFF
  public EngineeringUnitsSliderWidget(
      @Nonnull final EngineeringUnitsChangeListener listener,
      final double min,
      final double max,
      final double current,
      final double width,
      final String units,
      final boolean intCast) {
    this(listener, min, max, current, width, units);
    this.intCast = intCast;
  }

  public EngineeringUnitsSliderWidget(
      @Nonnull final EngineeringUnitsChangeListener listener,
      final double current,
      final double width,
      final String units) {
    this(listener, current / 2, current * 2, current, width, units);
  }

  public EngineeringUnitsSliderWidget(
      @Nonnull final EngineeringUnitsChangeListener listener,
      final double min,
      final double max,
      final double current,
      final double width,
      final String units) {
    super();
    this.listener = listener;
    setpoint = new Slider();

    double fixedMin = min;
    double fixedMax = max;

    if (fixedMin > fixedMax) {
      final double minStart = fixedMin;
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
    setpointValue.setOnAction(
        event ->
            Platform.runLater(
                () -> {
                  final double val = Double.parseDouble(setpointValue.getText());
                  setValue(val);
                  getListener().onSliderMoving(this, val);
                  getListener().onSliderDoneMoving(this, val);
                }));

    setpoint.setMaxWidth(width);
    setpoint
        .valueChangingProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              final double val = Double.parseDouble(setpointValue.getText());
              if (!newValue) {
                getListener().onSliderDoneMoving(this, val);
              }
            });
    setpoint.valueProperty().addListener(this);

    setAlignment(Pos.CENTER_LEFT);
    getColumnConstraints().add(new ColumnConstraints(width + 20));
    getColumnConstraints().add(new ColumnConstraints(100));

    final String unitsString = "(" + units + ")";
    getColumnConstraints().add(new ColumnConstraints(unitsString.length() * 7));

    add(setpoint, 0, 0);
    add(setpointValue, 1, 0);
    final Text unitText = new Text(unitsString);
    final HBox unitTextWrapper = new HBox(unitText); // Wrapper so we get padding
    unitTextWrapper.setPadding(new Insets(0, 0, 0, 5));
    unitTextWrapper.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(unitTextWrapper, Priority.NEVER);
    add(unitTextWrapper, 2, 0);
  }

  public void setUpperBound(final double newBound) {
    setpoint.setMax(newBound);
  }

  public void setLowerBound(final double newBound) {
    setpoint.setMin(newBound);
  }

  @Override
  public void changed(
      final ObservableValue<? extends Number> observable,
      final Number oldValue,
      final Number newValue) {
    updateValue();
  }

  private void updateValue() {
    Platform.runLater(
        () -> {
          setpointValue.setText(getFormatted(setpoint.getValue()));
          getListener().onSliderMoving(this, setpoint.getValue());
        });
  }

  /**
   * Set the value of this slider.
   *
   * @param value new value
   */
  public void setValue(final double value) {
    Platform.runLater(
        () -> {
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

          final double range = Math.abs(setpoint.getMax() - setpoint.getMin());
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
   * Format a value according to this slider'scale formatting rules.
   *
   * @param value the value to format
   * @return formatted version
   */
  public String getFormatted(final double value) {
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
        public void onSliderMoving(
            final EngineeringUnitsSliderWidget source, final double newAngleDegrees) {
          // Default is nothing
        }

        @Override
        public void onSliderDoneMoving(
            final EngineeringUnitsSliderWidget source, final double newAngleDegrees) {
          // Default is nothing
        }
      };
    }

    return listener;
  }

  public void setListener(final EngineeringUnitsChangeListener listener) {
    this.listener = listener;
  }

  public boolean isAllowResize() {
    return allowResize;
  }

  public void setAllowResize(final boolean allowResize) {
    this.allowResize = allowResize;
  }
  // CHECKSTYLE:ON
}
