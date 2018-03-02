package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;

public abstract class LimbSelection implements Selection {

  protected final DHParameterKinematics limb;

  public LimbSelection(final DHParameterKinematics limb) {
    this.limb = limb;
  }

}
