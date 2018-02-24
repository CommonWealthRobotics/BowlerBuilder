package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LimbTabLinkSelection extends LinkSelection {

  private final VBox vBox;

  public LimbTabLinkSelection(DHLink dhLink, LinkConfiguration configuration) {
    super(dhLink, configuration);
    vBox = new VBox(5);

    Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));

    vBox.getChildren().addAll(getTitleLabel(configuration.getName()), new VBox(5, removeLink));
  }

  @Override
  public Node getWidget() {
    return vBox;
  }

}
