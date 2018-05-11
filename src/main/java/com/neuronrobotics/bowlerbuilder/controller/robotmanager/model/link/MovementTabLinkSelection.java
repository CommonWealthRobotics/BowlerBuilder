/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.model.LinkData;
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.LinkSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MovementTabLinkSelection extends AbstractLinkSelection {

  private final LinkSliderWidget slider;

  public MovementTabLinkSelection(
      final int linkIndex,
      final DHLink dhLink,
      final LinkConfiguration configuration,
      final AbstractKinematicsNR device) {
    super(dhLink, configuration);

    slider = new LinkSliderWidget(linkIndex, dhLink, device);
  }

  public MovementTabLinkSelection(final LinkData newValue) {
    this(
        newValue.getIndex(),
        newValue.getDhLink(),
        newValue.getLinkConfiguration(),
        newValue.getDevice());
  }

  @Override
  public Node getWidget() {
    return slider;
  }
}
