/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.VirtualCameraDevice
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
    ): SelectionManager = DefaultSelectionManager(csgManager, focusGroup, virtualCam, moveCamera)
}
