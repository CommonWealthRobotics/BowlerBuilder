package com.neuronrobotics.bowlerbuilder.view.robotmanager;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsChangeListener;
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
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.reactfx.util.FxTimer;

@SuppressWarnings("restriction")
public class LinkSliderWidget extends Group implements IJInputEventListener,
    EngineeringUnitsChangeListener, ILinkListener {

  private AbstractKinematicsNR device;
  private DHParameterKinematics dhdevice;

  private int linkIndex;
  private EngineeringUnitsSliderWidget setpoint;
  private BowlerJInputDevice controller;
  private JogThread jogTHreadHandle;
  private double slider;
  private boolean stop;
  private double seconds;
  private String paramsKey;
  private AbstractLink abstractLink;

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
    panel.add(getSetpoint(), 2, 0);

    getChildren().add(panel);
    abstractLink.addLinkListener(this);
  }

  public void setUpperBound(double newBound) {
    getSetpoint().setUpperBound(newBound);
  }

  public void setLowerBound(double newBound) {
    getSetpoint().setLowerBound(newBound);
  }

  private void controllerLoop() {
    seconds = 0.1;

    if (getGameController() != null || !stop) {
      if (!stop) {
        jogTHreadHandle.setToSet(slider + getSetpoint().getValue(), seconds);
      }

      FxTimer.runLater(Duration.ofMillis((int) (seconds * 1000.0)), this::controllerLoop);
    }
  }

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
  public void onEvent(Component comp, net.java.games.input.Event event, float value,
      String eventString) {

    if (comp.getName().toLowerCase().contentEquals(
        (String) ConfigurationDatabase.getObject(paramsKey, "jogLink", "x"))) {
      slider = -value;
    }

    if (Math.abs(slider) < .01) {
      slider = 0;
    }

    stop = slider == 0;
  }

  @Override
  public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    try {
      device.setDesiredJointAxisValue(linkIndex, getSetpoint().getValue(), 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
  }

  @Override
  public void onLinkLimit(AbstractLink arg0, PIDLimitEvent arg1) {
  }

  @Override
  public void onLinkPositionUpdate(AbstractLink arg0, double arg1) {
    try {
      getSetpoint().setValue(arg1);
    } catch (ArrayIndexOutOfBoundsException ex) {
      return;
    }
  }

  public EngineeringUnitsSliderWidget getSetpoint() {
    return setpoint;
  }

  public void setSetpoint(EngineeringUnitsSliderWidget setpoint) {
    this.setpoint = setpoint;
  }

  private class JogThread extends Thread {

    private boolean controlThreadRunning = false;
    private double toSeconds = seconds;
    private double newValue;

    public void run() {
      setName("Jog Link Slider");

      while (device.isAvailable()) {
        if (controlThreadRunning) {
          try {
            device.setDesiredJointAxisValue(linkIndex, newValue, toSeconds);
            getSetpoint().setValue(newValue);
          } catch (Exception e) {
            e.printStackTrace();
          }

          controlThreadRunning = false;
        }

        ThreadUtil.wait((int) (toSeconds * 1000));
      }
    }

    public void setToSet(double newValue, double toSeconds) {
      this.newValue = newValue;
      this.toSeconds = toSeconds;
      controlThreadRunning = true;
    }

  }

}
