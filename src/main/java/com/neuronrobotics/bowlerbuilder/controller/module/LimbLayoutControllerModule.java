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
