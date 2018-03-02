package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.model.LinkData;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.LinkSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;

public class MovementTabLinkSelection extends AbstractLinkSelection {

  private final LinkSliderWidget slider;

  public MovementTabLinkSelection(final int linkIndex, final DHLink dhLink,
      final LinkConfiguration configuration,
      final AbstractKinematicsNR device) {
    super(dhLink, configuration);

    slider = new LinkSliderWidget(linkIndex, dhLink, device);
  }

  public MovementTabLinkSelection(final LinkData newValue) {
    this(newValue.index, newValue.dhLink, newValue.linkConfiguration, newValue.device);
  }

  @Override
  public Node getWidget() {
    return slider;
  }

}
