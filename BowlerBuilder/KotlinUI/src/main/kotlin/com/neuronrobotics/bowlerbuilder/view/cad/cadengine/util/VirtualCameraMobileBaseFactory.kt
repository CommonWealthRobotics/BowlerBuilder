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
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.util

import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.VirtualCameraDevice
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera.VirtualCameraMobileBase

object VirtualCameraMobileBaseFactory {

    /**
     * Creates a unique [VirtualCameraMobileBase] using the [virtualCam]'s hashcode as the device
     * name. This is necessary because the [VirtualCameraMobileBase] is classified as a hardware
     * device in the kernel, so it must be uniquely identified.
     *
     * @param virtualCam the [VirtualCameraDevice] that is logically attached to this
     * [VirtualCameraMobileBase]
     */
    @JvmStatic
    fun create(virtualCam: VirtualCameraDevice) =
            VirtualCameraMobileBase(
                    """
                    <root>
                    <mobilebase>

                    <driveType>none</driveType>
                    <name>FlyingCamera</name>
                    <appendage>
                    <name>BoomArm</name>
                    <link>
                    <name>boom</name>
                    <deviceName>${virtualCam.hashCode()}</deviceName>
                    <type>camera</type>
                    <index>0</index>

                    <scale>1</scale>
                    <upperLimit>255.0</upperLimit>
                    <lowerLimit>0.0</lowerLimit>
                    <upperVelocity>1.0E8</upperVelocity>
                    <lowerVelocity>-1.0E8</lowerVelocity>
                    <staticOffset>0</staticOffset>
                    <isLatch>false</isLatch>
                    <indexLatch>0</indexLatch>
                    <isStopOnLatch>false</isStopOnLatch>
                    <homingTPS>10000000</homingTPS>

                    <DHParameters>
                    <Delta>0</Delta>
                    <Theta>0.0</Theta>
                    <Radius>0</Radius>
                    <Alpha>0</Alpha>
                    </DHParameters>

                    </link>

                    <ZframeToRAS>
                    <x>0.0</x>
                    <y>0.0</y>
                    <z>0.0</z>
                    <rotw>1.0</rotw>
                    <rotx>0.0</rotx>
                    <roty>0.0</roty>
                    <rotz>0.0</rotz>
                    </ZframeToRAS>

                    <baseToZframe>
                    <x>0.0</x>
                    <y>0.0</y>
                    <z>0.0</z>
                    <rotw>1.0</rotw>
                    <rotx>0.0</rotx>
                    <roty>0.0</roty>
                    <rotz>0.0</rotz>
                    </baseToZframe>

                    </appendage>

                    <ZframeToRAS>
                    <x>0.0</x>
                    <y>0.0</y>
                    <z>0.0</z>
                    <rotw>1.0</rotw>
                    <rotx>0.0</rotx>
                    <roty>0.0</roty>
                    <rotz>0.0</rotz>
                    </ZframeToRAS>

                    <baseToZframe>
                    <x>0.0</x>
                    <y>0.0</y>
                    <z>0.0</z>
                    <rotw>1.0</rotw>
                    <rotx>0.0</rotx>
                    <roty>0.0</roty>
                    <rotz>0.0</rotz>
                    </baseToZframe>

                    </mobilebase>
                    </root>
                    """.trimIndent())
}
