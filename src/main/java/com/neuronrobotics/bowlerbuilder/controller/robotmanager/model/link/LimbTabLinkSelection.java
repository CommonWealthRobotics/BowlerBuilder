/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.CreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.LimbTabLimbSelection;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LimbTabLinkSelection extends AbstractLinkSelection {

  private final VBox vBox;

  public LimbTabLinkSelection(
      @Nullable final DHLink dhLink,
      final LinkConfiguration configuration,
      final DHParameterKinematics limb,
      final LimbTabLimbSelection limbTabLimbSelection,
      final CreatureEditorController creatureEditorController) {
    super(dhLink, configuration);
    vBox = new VBox(5);

    final Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
    removeLink.setOnAction(
        event -> {
          limb.removeLink(configuration.getLinkIndex());
          limbTabLimbSelection.clearSelectedWidget();
          limbTabLimbSelection.regenerateLinkButtons();
          creatureEditorController.regenerateMenus();
        });

    vBox.getChildren().addAll(getTitleLabel(configuration.getName()), new VBox(5, removeLink));
  }

  @Override
  public Node getWidget() {
    return vBox;
  }
}
