package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class ConfigTabLinkSelection extends LinkSelection {

  public ConfigTabLinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      AbstractKinematicsNR device) {
    super(linkIndex, dhLink, configuration, device);
  }

  @Override
  public Node getWidget() {
    return new AnchorPane();
  }

}
