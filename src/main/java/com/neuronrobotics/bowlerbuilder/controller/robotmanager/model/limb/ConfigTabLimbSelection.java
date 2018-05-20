/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.creatureeditor.TransformChangeListener;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.TransformWidget;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javafx.application.Platform;
import javafx.scene.Node;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConfigTabLimbSelection extends AbstractLimbSelection {

  private final TransformWidget widget;

  /**
   * Limb selection in the config tab.
   *
   * @param limb the limb
   * @param cadManager the CAD manager to regenerate CAD with
   */
  public ConfigTabLimbSelection(
      final DHParameterKinematics limb, final MobileBaseCadManager cadManager) {
    super(limb);

    widget =
        new TransformWidget(
            "Limb Configuration for " + limb.getScriptingName(),
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
                // this calls the render update function attached as the on joint
                // space update
                final double[] joint = limb.getCurrentJointSpaceVector();
                limb.getChain().getChain(joint);
                Platform.runLater(() -> limb.onJointSpaceUpdate(limb, joint));
              }
            });
  }

  @Nonnull
  @Override
  public Node getWidget() {
    return widget.getView();
  }
}
