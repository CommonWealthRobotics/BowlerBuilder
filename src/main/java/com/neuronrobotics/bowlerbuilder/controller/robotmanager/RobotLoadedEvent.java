package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.neuronrobotics.sdk.addons.kinematics.MobileBase;

public class RobotLoadedEvent {

  public MobileBase device;

  public RobotLoadedEvent(MobileBase device) {
    this.device = device;
  }

}
