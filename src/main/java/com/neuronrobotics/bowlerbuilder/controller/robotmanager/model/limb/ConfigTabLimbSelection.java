package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.creatureeditor.TransformChangeListener;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.TransformWidget;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javafx.application.Platform;
import javafx.scene.Node;

public class ConfigTabLimbSelection extends AbstractLimbSelection {

  private final TransformWidget widget;

  public ConfigTabLimbSelection(final DHParameterKinematics limb, final MobileBase device,
      final MobileBaseCadManager cadManager) {
    super(limb);

    widget = new TransformWidget("Limb Configuration for " + limb.getScriptingName(),
        limb.getRobotToFiducialTransform(),
        new TransformChangeListener() {
          @Override
          public void onTransformFinished(final TransformNR newTrans) {
            cadManager.generateCad();
          }

          @Override
          public void onTransformChanging(final TransformNR newTrans) {
            limb.setRobotToFiducialTransform(newTrans);
            limb.getCurrentTaskSpaceTransform();
            //this calls the render update function attached as the on joint space update
            final double[] joint = limb.getCurrentJointSpaceVector();
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
