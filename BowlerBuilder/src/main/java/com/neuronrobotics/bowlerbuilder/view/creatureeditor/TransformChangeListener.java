/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.creatureeditor;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface TransformChangeListener {

  void onTransformChanging(TransformNR newTrans);

  void onTransformFinished(TransformNR newTrans);
}
