package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class LimbSelection implements Selection {

  private DHParameterKinematics limb;

  public LimbSelection(DHParameterKinematics limb) {
    this.limb = limb;
  }

  @Override
  public Node getWidget() {
    return new Label(limb.getScriptingName() + " selected!");
//    return new JogWidget();
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
