/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.creatureeditor;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DhLinkType;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javax.annotation.ParametersAreNonnullByDefault;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import org.reactfx.util.FxTimer;

@SuppressWarnings("restriction")
@ParametersAreNonnullByDefault
public class LinkSliderWidget extends Group
    implements IJInputEventListener, EngineeringUnitsChangeListener, ILinkListener {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(LinkSliderWidget.class.getSimpleName());
  private final AbstractKinematicsNR device;
  private final int linkIndex;
  private final AbstractLink abstractLink;

  private EngineeringUnitsSliderWidget setpoint;
  private BowlerJInputDevice controller;
  private JogThread jogTHreadHandle;
  private double slider;
  private boolean stop;
  private double seconds;
  private String paramsKey;

  /**
   * From BowlerStudio.
   *
   * @param linkIndex hardware index
   * @param dhlink DH link
   * @param abstractKinematicsNR attached device
   */
  public LinkSliderWidget(
      final int linkIndex, final DHLink dhlink, final AbstractKinematicsNR abstractKinematicsNR) {
    super();
    this.linkIndex = linkIndex;
    this.device = abstractKinematicsNR;

    abstractLink = device.getAbstractLink(linkIndex);

    final TextField name = new TextField(abstractLink.getLinkConfiguration().getName());
    name.setMaxWidth(100.0);
    name.setOnAction(event -> abstractLink.getLinkConfiguration().setName(name.getText()));

    setSetpoint(
        new EngineeringUnitsSliderWidget(
            this,
            abstractLink.getMinEngineeringUnits(),
            abstractLink.getMaxEngineeringUnits(),
            device.getCurrentJointSpaceVector()[linkIndex],
            180,
            dhlink.getLinkType() == DhLinkType.ROTORY ? "degrees" : "mm"));

    final GridPane panel = new GridPane();

    panel.getColumnConstraints().add(new ColumnConstraints(30));
    panel.getColumnConstraints().add(new ColumnConstraints(120));
    panel.getColumnConstraints().add(new ColumnConstraints(120));

    panel.add(new Text("#" + linkIndex), 0, 0);
    panel.add(name, 1, 0);
    panel.add(setpoint, 2, 0);

    getChildren().add(panel);
    abstractLink.addLinkListener(this);
  }

  public void setUpperBound(final double newBound) {
    setpoint.setUpperBound(newBound);
  }

  public void setLowerBound(final double newBound) {
    setpoint.setLowerBound(newBound);
  }

  private void controllerLoop() {
    seconds = 0.1;

    if (getGameController() != null || !stop) {
      if (!stop) {
        jogTHreadHandle.setToSet(slider + setpoint.getValue(), seconds);
      }

      FxTimer.runLater(Duration.ofMillis((int) (seconds * 1000.0)), this::controllerLoop);
    }
  }

  /**
   * Use a controller instead of the buttons.
   *
   * @param controller {@link BowlerJInputDevice} controller
   */
  public void setGameController(final BowlerJInputDevice controller) {
    this.controller = controller;

    if (jogTHreadHandle == null) {
      jogTHreadHandle = new JogThread();
      jogTHreadHandle.start();
    }

    final Controller hwController = controller.getController();
    paramsKey = hwController.getName();
    getGameController().clearListeners();
    getGameController().addListeners(this);
    controllerLoop();
  }

  public BowlerJInputDevice getGameController() {
    return controller;
  }

  @Override
  public void onEvent(
      final Component comp, final Event event, final float value, final String eventString) {

    if (comp.getName()
        .toLowerCase(Locale.ENGLISH)
        .contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogLink", "x"))) {
      slider = -value;
    }

    if (Math.abs(slider) < .01) {
      slider = 0;
    }

    stop = slider == 0;
  }

  @Override
  public void onSliderMoving(
      final EngineeringUnitsSliderWidget source, final double newAngleDegrees) {
    final double value = setpoint.getValue();
    try {
      device.setDesiredJointAxisValue(linkIndex, value, 0);
    } catch (final Exception e) {
      LOGGER.log(
          Level.WARNING,
          "Could not set new joint axis value of "
              + value
              + ".\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  @Override
  public void onSliderDoneMoving(
      final EngineeringUnitsSliderWidget source, final double newAngleDegrees) {
    // Don'translate need to implement
  }

  @Override
  public void onLinkLimit(final AbstractLink arg0, final PIDLimitEvent arg1) {
    // Don'translate need to implement
  }

  @Override
  public void onLinkPositionUpdate(final AbstractLink arg0, final double arg1) {
    setpoint.setValue(arg1);
  }

  public EngineeringUnitsSliderWidget getSetpoint() {
    return setpoint;
  }

  public void setSetpoint(final EngineeringUnitsSliderWidget setpoint) {
    this.setpoint = setpoint;
  }

  @ParametersAreNonnullByDefault
  private class JogThread extends Thread {

    private boolean controlThreadRunning;
    private double toSeconds = seconds;
    private double newValue;

    JogThread() {
      super();
    }

    @Override
    public void run() {
      setName("Jog Link Slider");

      while (device.isAvailable()) {
        if (controlThreadRunning) {
          try {
            device.setDesiredJointAxisValue(linkIndex, newValue, toSeconds);
            getSetpoint().setValue(newValue);
          } catch (final Exception e) {
            LOGGER.log(
                Level.WARNING,
                "Could not set new joint axis value of "
                    + newValue
                    + ".\n"
                    + Throwables.getStackTraceAsString(e));
          }

          controlThreadRunning = false;
        }

        ThreadUtil.wait((int) (toSeconds * 1000));
      }
    }

    private void setToSet(final double newValue, final double toSeconds) {
      this.newValue = newValue;
      this.toSeconds = toSeconds;
      controlThreadRunning = true;
    }
  }
}
