package com.neuronrobotics.bowlerbuilder.controller.cadengine.view;

public interface EngineeringUnitsChangeListener {

  void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

  void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

}
