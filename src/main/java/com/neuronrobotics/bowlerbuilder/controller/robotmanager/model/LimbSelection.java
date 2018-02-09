package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.scene.Node;

public class LimbSelection implements Selection {

  private DHParameterKinematics limb;
  private JogWidget jogWidget;

  public LimbSelection(DHParameterKinematics limb) {
    this.limb = limb;
    jogWidget = new JogWidget(limb);
  }

  @Override
  public Node getWidget() {
    return jogWidget.getView();
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
