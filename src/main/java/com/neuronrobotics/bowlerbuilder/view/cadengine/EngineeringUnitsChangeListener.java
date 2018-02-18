package com.neuronrobotics.bowlerbuilder.view.cadengine;

public interface EngineeringUnitsChangeListener {

  void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

  void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees);

}
