package com.neuronrobotics.bowlerbuilder.view.creatureeditor;


import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

public interface TransformChangeListener {

  void onTransformChanging(TransformNR newTrans);

  void onTransformFinished(TransformNR newTrans);

}
