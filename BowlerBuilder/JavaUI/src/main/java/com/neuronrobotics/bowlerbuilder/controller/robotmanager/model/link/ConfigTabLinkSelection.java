/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.DHSettingsWidget;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.LinkConfigurationWidget;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConfigTabLinkSelection extends AbstractLinkSelection
    implements EngineeringUnitsChangeListener {

  private final MobileBaseCadManager cadManager;
  private final VBox widget;

  /**
   * Limb selection in the config tab.
   *
   * @param dhLink the link
   * @param configuration the link's configuration
   * @param device the device the link is attached to
   * @param cadManager the CAD manager to regenerate CAD with
   */
  public ConfigTabLinkSelection(
      final DHLink dhLink,
      final LinkConfiguration configuration,
      final DHParameterKinematics device,
      final MobileBaseCadManager cadManager) {
    super(dhLink, configuration);
    this.cadManager = cadManager;

    widget = new VBox(5);
    widget.setPadding(new Insets(5));

    final DHSettingsWidget dhSettings =
        new DHSettingsWidget(
            "D-H Configuration for " + configuration.getName(), dhLink, device, this);
    widget.getChildren().add(dhSettings.getView());

    widget.getChildren().add(getTitleLabel("Link Configuration for " + configuration.getName()));
    final LinkConfigurationWidget linkConfig =
        new LinkConfigurationWidget(configuration, device.getFactory(), null);
    widget.getChildren().add(linkConfig);
  }

  @Nonnull
  @Override
  public Node getWidget() {
    return widget;
  }

  @Override
  public void onSliderMoving(
      final EngineeringUnitsSliderWidget source, final double newAngleDegrees) {
    // Don't need to implement
  }

  @Override
  public void onSliderDoneMoving(
      final EngineeringUnitsSliderWidget source, final double newAngleDegrees) {
    cadManager.generateCad();
  }
}
