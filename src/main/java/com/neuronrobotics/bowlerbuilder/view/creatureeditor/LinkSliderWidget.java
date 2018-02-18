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
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
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
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import org.reactfx.util.FxTimer;

@SuppressWarnings("restriction")
public class LinkSliderWidget extends Group implements IJInputEventListener,
    EngineeringUnitsChangeListener, ILinkListener {

  private static final Logger logger =
      LoggerUtilities.getLogger(LinkSliderWidget.class.getSimpleName());
  private final AbstractKinematicsNR device;
  private final int linkIndex;
  private final AbstractLink abstractLink;

  private DHParameterKinematics dhdevice;
  private EngineeringUnitsSliderWidget setpoint;
  private BowlerJInputDevice controller;
  private JogThread jogTHreadHandle;
  private double slider;
  private boolean stop;
  private double seconds;
  private String paramsKey;

  public LinkSliderWidget(int linkIndex, DHLink dhlink, AbstractKinematicsNR d) {
    this.linkIndex = linkIndex;
    this.device = d;
    if (DHParameterKinematics.class.isInstance(device)) {
      dhdevice = (DHParameterKinematics) device;
    }

    abstractLink = device.getAbstractLink(linkIndex);

    TextField name = new TextField(abstractLink.getLinkConfiguration().getName());
    name.setMaxWidth(100.0);
    name.setOnAction(event -> abstractLink.getLinkConfiguration().setName(name.getText()));

    setSetpoint(new EngineeringUnitsSliderWidget(this,
        abstractLink.getMinEngineeringUnits(),
        abstractLink.getMaxEngineeringUnits(),
        device.getCurrentJointSpaceVector()[linkIndex],
        180,
        dhlink.getLinkType() == DhLinkType.ROTORY ? "degrees" : "mm"));

    GridPane panel = new GridPane();

    panel.getColumnConstraints().add(new ColumnConstraints(30));
    panel.getColumnConstraints().add(new ColumnConstraints(120));
    panel.getColumnConstraints().add(new ColumnConstraints(120));

    panel.add(new Text("#" + linkIndex), 0, 0);
    panel.add(name, 1, 0);
    panel.add(setpoint, 2, 0);

    getChildren().add(panel);
    abstractLink.addLinkListener(this);
  }

  public void setUpperBound(double newBound) {
    setpoint.setUpperBound(newBound);
  }

  public void setLowerBound(double newBound) {
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
  public void setGameController(BowlerJInputDevice controller) {
    this.controller = controller;

    if (controller != null && jogTHreadHandle == null) {
      jogTHreadHandle = new JogThread();
      jogTHreadHandle.start();
    }

    if (controller != null) {
      Controller hwController = controller.getController();
      paramsKey = hwController.getName();
      getGameController().clearListeners();
      getGameController().addListeners(this);
      controllerLoop();
    }
  }

  public BowlerJInputDevice getGameController() {
    return controller;
  }

  @Override
  public void onEvent(Component comp, Event event, float value, String eventString) {

    if (comp.getName().toLowerCase(Locale.ENGLISH)
        .contentEquals((String) ConfigurationDatabase.getObject(paramsKey, "jogLink", "x"))) {
      slider = -value;
    }

    if (Math.abs(slider) < .01) {
      slider = 0;
    }

    stop = slider == 0;
  }

  @Override
  public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    double value = setpoint.getValue();
    try {
      device.setDesiredJointAxisValue(linkIndex, value, 0);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Could not set new joint axis value of " + value + ".\n"
          + Throwables.getStackTraceAsString(e));
    }
  }

  @Override
  public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    //Don't need to implement
  }

  @Override
  public void onLinkLimit(AbstractLink arg0, PIDLimitEvent arg1) {
    //Don't need to implement
  }

  @Override
  public void onLinkPositionUpdate(AbstractLink arg0, double arg1) {
    setpoint.setValue(arg1);
  }

  public EngineeringUnitsSliderWidget getSetpoint() {
    return setpoint;
  }

  public void setSetpoint(EngineeringUnitsSliderWidget setpoint) {
    this.setpoint = setpoint;
  }

  private class JogThread extends Thread {

    private boolean controlThreadRunning;
    private double toSeconds = seconds;
    private double newValue;

    @Override
    public void run() {
      setName("Jog Link Slider");

      while (device.isAvailable()) {
        if (controlThreadRunning) {
          try {
            device.setDesiredJointAxisValue(linkIndex, newValue, toSeconds);
            getSetpoint().setValue(newValue);
          } catch (Exception e) {
            logger.log(Level.WARNING, "Could not set new joint axis value of " + newValue + ".\n"
                + Throwables.getStackTraceAsString(e));
          }

          controlThreadRunning = false;
        }

        ThreadUtil.wait((int) (toSeconds * 1000));
      }
    }

    private void setToSet(double newValue, double toSeconds) {
      this.newValue = newValue;
      this.toSeconds = toSeconds;
      controlThreadRunning = true;
    }

  }

}
