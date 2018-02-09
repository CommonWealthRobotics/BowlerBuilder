package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.scene.Node;

public class LimbSelection implements Selection {

  private DHParameterKinematics limb;

  public LimbSelection(DHParameterKinematics limb) {
    this.limb = limb;
  }

  @Override
  public Node getWidget() {
    return new JogWidget(limb);
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
