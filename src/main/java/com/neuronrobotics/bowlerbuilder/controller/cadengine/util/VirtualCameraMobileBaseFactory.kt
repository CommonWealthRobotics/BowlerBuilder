/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.util

import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraMobileBase

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
