/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera

import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import java.io.ByteArrayInputStream
import java.util.ArrayList
import javax.annotation.ParametersAreNonnullByDefault

class VirtualCameraMobileBase(text: String) : MobileBase(
    ByteArrayInputStream(text.toByteArray())
) {

    private val bases = ArrayList<VirtualCameraMobileBase>()

    var driveEngine: IDriveEngine = IDriveEngineImplementation()
        set(driveEngine) {
            field = driveEngine
            for (base in bases) {
                base.setWalkingDriveEngine(driveEngine)
            }
        }

    init {
        setWalkingDriveEngine(driveEngine)
        bases.add(this)
    }

    @ParametersAreNonnullByDefault
    private class IDriveEngineImplementation : IDriveEngine {

        private val pureTrans = TransformNR()

        /** Not used.  */
        override fun DriveVelocityStraight(source: MobileBase, cmPerSecond: Double) {
            // Not used
        }

        /** Not used.  */
        override fun DriveVelocityArc(
            source: MobileBase,
            degreesPerSecond: Double,
            cmRadius: Double
        ) {
            // Not used
        }

        /**
         * Move in an arc.
         *
         * @param source base to move
         * @param newPose transform to move on
         * @param seconds time to move over
         */
        override fun DriveArc(source: MobileBase, newPose: TransformNR, seconds: Double) {
            pureTrans.x = newPose.x
            pureTrans.y = newPose.y
            pureTrans.z = newPose.z

            val global = source.fiducialToGlobalTransform.times(pureTrans)
            global.rotation = RotationNR(
                Math.toDegrees(
                    newPose.rotation.rotationTilt + global.rotation.rotationTilt
                ) % 360,
                Math.toDegrees(
                    newPose.rotation.rotationAzimuth + global.rotation.rotationAzimuth
                ) % 360,
                Math.toDegrees(
                    newPose.rotation.rotationElevation + global.rotation.rotationElevation
                )
            )

            source.setGlobalToFiducialTransform(global)
        }
    }
}
