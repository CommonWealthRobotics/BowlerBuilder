package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.model.LinkDataPair;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.LinkSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;

public class MovementTabLinkSelection extends LinkSelection {

  private final LinkSliderWidget slider;

  public MovementTabLinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      AbstractKinematicsNR device) {
    super(dhLink, configuration);

    slider = new LinkSliderWidget(linkIndex, dhLink, device);
  }

  public MovementTabLinkSelection(LinkDataPair newValue) {
    this(newValue.index, newValue.dhLink, newValue.linkConfiguration, newValue.device);
  }

  @Override
  public Node getWidget() {
    return slider;
  }

}
