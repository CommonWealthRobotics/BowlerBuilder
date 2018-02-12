package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LimbTabLinkSelection extends LinkSelection {

  private final VBox vBox;

  public LimbTabLinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      AbstractKinematicsNR device) {
    super(linkIndex, dhLink, configuration, device);

    Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
    vBox = new VBox(5, getTitleLabel(configuration.getName()), removeLink);
  }

  @Override
  public Node getWidget() {
    return vBox;
  }

}
