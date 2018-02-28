package com.neuronrobotics.bowlerbuilder.model;

public enum LimbType {

  LEG("defaultLeg.xml"),
  ARM("defaultArm.xml"),
  FIXED_WHEEL("defaultFixed.xml"),
  STEERABLE_WHEEL("defaultSteerable.xml");

  private final String defaultFileName;

  LimbType(String defaultFileName) {
    this.defaultFileName = defaultFileName;
  }

  public String getDefaultFileName() {
    return defaultFileName;
  }

}
