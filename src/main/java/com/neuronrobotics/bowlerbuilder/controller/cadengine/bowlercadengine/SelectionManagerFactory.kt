/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine

import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import javafx.scene.Group
import java.util.function.BiConsumer

class SelectionManagerFactory {

    /**
     * Creates a [DefaultSelectionManager].
     *
     * @param csgManager [CSGManager] to pull the CSG map from
     * @param focusGroup focus group
     * @param virtualCam virtual camera
     * @param moveCamera [BiConsumer] to move the camera around for a new selection
     * @return a [DefaultSelectionManager]
     */
    fun create(
        csgManager: CSGManager,
        focusGroup: Group,
        virtualCam: VirtualCameraDevice,
        moveCamera: BiConsumer<TransformNR, Double>
    ): SelectionManager {
        return DefaultSelectionManager(csgManager, focusGroup, virtualCam, moveCamera)
    }
}
