package com.neuronrobotics.bowlerbuilder.controller.cadengine.view;

public interface OnEngineeringUnitsChange {

  void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

  void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

}
