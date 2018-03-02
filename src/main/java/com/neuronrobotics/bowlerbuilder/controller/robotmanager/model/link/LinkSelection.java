package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

public abstract class LinkSelection implements Selection {

  protected final DHLink dhLink;
  protected final LinkConfiguration configuration;

  /**
   * Any link selection should use a {@link DHLink} and a {@link LinkConfiguration}.
   *
   * @param dhLink link
   * @param configuration configuration
   */
  public LinkSelection(final DHLink dhLink, final LinkConfiguration configuration) {
    this.dhLink = dhLink;
    this.configuration = configuration;
  }

  public DHLink getDhLink() {
    return dhLink;
  }

  public LinkConfiguration getConfiguration() {
    return configuration;
  }

}
