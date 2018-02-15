package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

public abstract class LinkSelection implements Selection {

  protected int linkIndex;
  protected DHLink dhLink;
  protected LinkConfiguration configuration;
  protected AbstractKinematicsNR device;

  public LinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      AbstractKinematicsNR device) {
    this.linkIndex = linkIndex;
    this.dhLink = dhLink;
    this.configuration = configuration;
    this.device = device;
  }

}
