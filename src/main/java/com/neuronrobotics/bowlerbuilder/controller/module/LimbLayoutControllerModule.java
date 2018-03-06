/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import javax.annotation.Nonnull;

public class LimbLayoutControllerModule extends AbstractModule {

  private final MobileBase device;

  public LimbLayoutControllerModule(@Nonnull final MobileBase device) {
    this.device = device;
  }

  @Override
  protected void configure() {
    bind(MobileBase.class).toInstance(device);
  }

}
