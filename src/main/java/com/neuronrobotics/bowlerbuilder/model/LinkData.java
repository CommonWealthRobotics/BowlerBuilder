/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.model;

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;

public class LinkData {

  public final DHParameterKinematics parentLimb;
  public final Integer index;
  public final DHLink dhLink;
  public final LinkConfiguration linkConfiguration;
  public final AbstractKinematicsNR device;

  public LinkData(final DHParameterKinematics parentLimb, final Integer index, final DHLink dhLink,
      final LinkConfiguration linkConfiguration, final AbstractKinematicsNR device) {
    this.parentLimb = parentLimb;
    this.index = index;
    this.dhLink = dhLink;
    this.linkConfiguration = linkConfiguration;
    this.device = device;
  }

}
