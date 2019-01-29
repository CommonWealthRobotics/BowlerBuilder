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
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera

import com.neuronrobotics.imageprovider.AbstractImageProvider
import com.neuronrobotics.sdk.addons.kinematics.TransformFactory
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import javafx.scene.Camera
import javafx.scene.Group
import javafx.scene.transform.Affine
import java.awt.image.BufferedImage
import java.util.ArrayList

class VirtualCameraDevice(
    camera: Camera,
    hand: Group
) : AbstractImageProvider() {

    private val zoomAffine = Affine()
    private val manipulationFrame: Group

    val cameraFrame = Group()
    var zoomDepth = defaultZoomDepth.toDouble()
        set(newDepth) {
            field = newDepth

            if (this.zoomDepth > -2) {
                field = -2.0
            }

            if (this.zoomDepth < -5000) {
                field = -5000.0
            }

            zoomAffine.tz = this.zoomDepth
        }

    init {
        scriptingName = "virtualCameraDevice"

        manipulationFrame = Group()
        camera.transforms.add(zoomAffine)

        cameraFrame.transforms.add(offset)
        manipulationFrame.children.addAll(camera, hand)
        cameraFrame.children.add(manipulationFrame)
    }

    override fun setGlobalPositionListener(affine: Affine) {
        super.setGlobalPositionListener(affine)
        manipulationFrame.transforms.clear()
        manipulationFrame.transforms.add(affine)
    }

    override fun captureNewImage(imageData: BufferedImage): Boolean = false

    override fun captureNewImage(): BufferedImage? = null

    /** Nothing to disconnect.  */
    override fun disconnectDeviceImp() {
        // Not used
    }

    /**
     * Nothing to connect.
     *
     * @return true
     */
    override fun connectDeviceImp() = true

    override fun getNamespacesImp(): ArrayList<String> = ArrayList()

    companion object {
        const val defaultZoomDepth = -1500
        val offset: Affine = TransformFactory.nrToAffine(
            TransformNR(
                0.0,
                0.0,
                0.0,
                RotationNR(180.0, 0.0, 0.0)
            )
        )
    }
}
