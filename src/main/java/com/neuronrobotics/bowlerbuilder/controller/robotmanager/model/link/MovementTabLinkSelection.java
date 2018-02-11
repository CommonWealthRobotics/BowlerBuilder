package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.LinkSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;

public class MovementTabLinkSelection extends LinkSelection {

  private final LinkSliderWidget slider;

  public MovementTabLinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      AbstractKinematicsNR device) {
    super(linkIndex, dhLink, configuration, device);

    slider = new LinkSliderWidget(linkIndex, dhLink, device);
  }

  @Override
  public Node getWidget() {
    return slider;
  }

}
