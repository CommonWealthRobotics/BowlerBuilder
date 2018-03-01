package com.neuronrobotics.bowlerbuilder.model;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

public class LinkData {

  public final DHParameterKinematics parentLimb;
  public final Integer index;
  public final DHLink dhLink;
  public final LinkConfiguration linkConfiguration;
  public final AbstractKinematicsNR device;

  public LinkData(DHParameterKinematics parentLimb, Integer index, DHLink dhLink,
      LinkConfiguration linkConfiguration, AbstractKinematicsNR device) {
    this.parentLimb = parentLimb;
    this.index = index;
    this.dhLink = dhLink;
    this.linkConfiguration = linkConfiguration;
    this.device = device;
  }

}
