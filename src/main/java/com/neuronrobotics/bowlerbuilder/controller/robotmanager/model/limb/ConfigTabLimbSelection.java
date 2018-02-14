package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.TransformChangeListener;
import com.neuronrobotics.bowlerbuilder.view.robotmanager.TransformWidget;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javafx.application.Platform;
import javafx.scene.Node;

public class ConfigTabLimbSelection extends LimbSelection {

  private final TransformWidget widget;

  public ConfigTabLimbSelection(DHParameterKinematics limb, MobileBase device,
      MobileBaseCadManager cadManager) {
    super(limb);

    widget = new TransformWidget("Limb Configuration for " + limb.getScriptingName(),
        limb.getRobotToFiducialTransform(),
        new TransformChangeListener() {
          @Override
          public void onTransformFinished(TransformNR newTrans) {
            cadManager.generateCad();
          }

          @Override
          public void onTransformChanging(TransformNR newTrans) {
            limb.setRobotToFiducialTransform(newTrans);
            limb.getCurrentTaskSpaceTransform();
            //this calls the render update function attached as the on joint space update
            double[] joint = limb.getCurrentJointSpaceVector();
            limb.getChain().getChain(joint);
            Platform.runLater(() -> limb.onJointSpaceUpdate(limb, joint));
          }
        });
  }

  @Override
  public Node getWidget() {
    return widget.getView();
  }

}
