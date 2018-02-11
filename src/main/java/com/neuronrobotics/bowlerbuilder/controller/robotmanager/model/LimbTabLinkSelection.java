package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LimbTabLinkSelection implements Selection {

  private final VBox vBox;

  public LimbTabLinkSelection(LinkConfiguration link) {
    Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
    vBox = new VBox(5, getLabel(link.getName()), removeLink);
  }

  @Override
  public Node getWidget() {
    return vBox;
  }

}
