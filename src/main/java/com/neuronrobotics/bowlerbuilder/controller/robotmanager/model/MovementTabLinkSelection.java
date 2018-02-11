package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.LinkSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import javafx.scene.Node;

public class MovementTabLinkSelection implements Selection {

  private final LinkSliderWidget slider;

  public MovementTabLinkSelection(int linkIndex, DHLink dhlink, AbstractKinematicsNR d) {
    slider = new LinkSliderWidget(linkIndex, dhlink, d);
  }

  @Override
  public Node getWidget() {
    return slider;
  }

}
