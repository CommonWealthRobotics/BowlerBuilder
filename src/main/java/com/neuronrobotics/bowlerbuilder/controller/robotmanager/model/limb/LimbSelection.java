package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;

public abstract class LimbSelection implements Selection {

  protected DHParameterKinematics limb;

  public LimbSelection(DHParameterKinematics limb) {
    this.limb = limb;
  }

}
