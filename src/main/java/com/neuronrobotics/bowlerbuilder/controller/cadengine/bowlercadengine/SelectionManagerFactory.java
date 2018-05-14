/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine;

import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.util.function.BiConsumer;
import javafx.scene.Group;

public class SelectionManagerFactory {

  public SelectionManagerFactory() {}

  /**
   * Creates a {@link DefaultSelectionManager}.
   *
   * @param csgManager {@link CSGManager} to pull the CSG map from
   * @param focusGroup focus group
   * @param virtualCam virtual camera
   * @param moveCamera {@link BiConsumer} to move the camera around for a new selection
   * @return a {@link DefaultSelectionManager}
   */
  public SelectionManager create(
      final CSGManager csgManager,
      final Group focusGroup,
      final VirtualCameraDevice virtualCam,
      final BiConsumer<TransformNR, Double> moveCamera) {
    return new DefaultSelectionManager(csgManager, focusGroup, virtualCam, moveCamera);
  }
}
