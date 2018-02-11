package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class ConfigTabLimbSelection extends LimbSelection {

  public ConfigTabLimbSelection(DHParameterKinematics limb) {
    super(limb);
  }

  @Override
  public Node getWidget() {
    return new AnchorPane();
  }

}
