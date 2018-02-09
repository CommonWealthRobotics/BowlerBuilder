package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class LinkSelection implements Selection {

  private LinkConfiguration link;

  public LinkSelection(LinkConfiguration link) {
    this.link = link;
  }

  @Override
  public Node getWidget() {
    return new Label(link.getName() + " selected!");
  }

  public LinkConfiguration getLink() {
    return link;
  }

}
