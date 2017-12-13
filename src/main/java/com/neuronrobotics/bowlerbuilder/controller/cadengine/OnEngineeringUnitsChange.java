package com.neuronrobotics.bowlerbuilder.controller.cadengine;

public interface OnEngineeringUnitsChange {

  void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

  void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

}
