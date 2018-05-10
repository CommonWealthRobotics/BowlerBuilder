/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

public abstract class AbstractLinkSelection implements Selection {

  protected final DHLink dhLink;
  protected final LinkConfiguration configuration;

  /**
   * Any link selection should use a {@link DHLink} and a {@link LinkConfiguration}.
   *
   * @param dhLink link
   * @param configuration configuration
   */
  public AbstractLinkSelection(final DHLink dhLink, final LinkConfiguration configuration) {
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
