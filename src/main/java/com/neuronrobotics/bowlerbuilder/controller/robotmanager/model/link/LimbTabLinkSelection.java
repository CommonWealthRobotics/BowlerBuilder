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
import javax.annotation.Nonnull;

public class LimbTabLinkSelection extends AbstractLinkSelection {

  private final VBox vBox;

  public LimbTabLinkSelection(final DHLink dhLink, @Nonnull final LinkConfiguration configuration,
      @Nonnull final DHParameterKinematics limb,
      @Nonnull final LimbTabLimbSelection limbTabLimbSelection,
      @Nonnull final CreatureEditorController creatureEditorController) {
    super(dhLink, configuration);
    vBox = new VBox(5);

    final Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
    removeLink.setOnAction(event -> {
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
