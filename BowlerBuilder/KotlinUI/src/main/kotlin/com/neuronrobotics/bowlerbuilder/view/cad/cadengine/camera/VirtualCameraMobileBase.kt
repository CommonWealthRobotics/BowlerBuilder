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
