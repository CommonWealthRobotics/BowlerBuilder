package com.neuronrobotics.bowlerbuilder.model;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

public class LinkDataPair {

  public Integer index;
  public DHLink dhLink;
  public LinkConfiguration linkConfiguration;
  public AbstractKinematicsNR device;

  public LinkDataPair(Integer index, DHLink dhLink, LinkConfiguration linkConfiguration,
      AbstractKinematicsNR device) {
    this.index = index;
    this.dhLink = dhLink;
    this.linkConfiguration = linkConfiguration;
    this.device = device;
  }

}
