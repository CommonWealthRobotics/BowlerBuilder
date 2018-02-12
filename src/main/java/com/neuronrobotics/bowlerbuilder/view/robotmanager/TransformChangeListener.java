package com.neuronrobotics.bowlerbuilder.view.robotmanager;


import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

public interface TransformChangeListener {

  void onTransformChanging(TransformNR newTrans);

  void onTransformFinished(TransformNR newTrans);

}
