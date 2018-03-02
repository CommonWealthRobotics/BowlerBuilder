package com.neuronrobotics.bowlerbuilder.model;

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

}
