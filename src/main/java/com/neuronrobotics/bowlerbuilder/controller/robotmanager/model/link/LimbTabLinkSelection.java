package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.LimbTabLimbSelection;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LimbTabLinkSelection extends LinkSelection {

  private final VBox vBox;

  public LimbTabLinkSelection(DHLink dhLink, LinkConfiguration configuration,
      DHParameterKinematics limb, LimbTabLimbSelection limbTabLimbSelection) {
    super(dhLink, configuration);
    vBox = new VBox(5);

    Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
    removeLink.setOnAction(event -> {
      limb.removeLink(configuration.getLinkIndex()); //TODO: Fix remove link
      limbTabLimbSelection.clearSelectedWidget();
    });

    vBox.getChildren().addAll(getTitleLabel(configuration.getName()), new VBox(5, removeLink));
  }

  @Override
  public Node getWidget() {
    return vBox;
  }

}
