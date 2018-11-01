/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public enum LimbType {
  LEG("defaultLeg.xml"),
  ARM("defaultArm.xml"),
  FIXED_WHEEL("defaultFixed.xml"),
  STEERABLE_WHEEL("defaultSteerable.xml");

  private final String defaultFileName;

  LimbType(final String defaultFileName) {
    this.defaultFileName = defaultFileName;
  }

  public String getDefaultFileName() {
    return defaultFileName;
  }

  public String getTooltipName() {
    switch (this) {
      case LEG:
        return "Leg";
      case ARM:
        return "Arm";
      case FIXED_WHEEL:
        return "Fixed Wheel";
      case STEERABLE_WHEEL:
        return "Steerable Wheel";
      default:
        return "Unknown Limb Type";
    }
  }
}
