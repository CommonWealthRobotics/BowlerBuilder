/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.util;

import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraMobileBase;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class VirtualCameraMobileBaseFactory {

  @Nonnull
  public static VirtualCameraMobileBase create(final VirtualCameraDevice virtualCam) {
    return new VirtualCameraMobileBase(
        "<root>\n"
            + "<mobilebase>\n"
            + "<driveType>none</driveType>\n"
            + "<name>FlyingCamera</name>\n"
            + "<appendage>\n"
            + "<name>BoomArm</name>\n"
            + "<link>\n"
            + "\t<name>boom</name>\n"
            + "\t<deviceName>"
            + virtualCam.hashCode()
            + "</deviceName>\n"
            + "\t<type>camera</type>\n"
            + "\t<index>0</index>\n"
            + "\t\n"
            + "\t<scale>1</scale>\n"
            + "\t<upperLimit>255.0</upperLimit>\n"
            + "\t<lowerLimit>0.0</lowerLimit>\n"
            + "\t<upperVelocity>1.0E8</upperVelocity>\n"
            + "\t<lowerVelocity>-1.0E8</lowerVelocity>\n"
            + "\t<staticOffset>0</staticOffset>\n"
            + "\t<isLatch>false</isLatch>\n"
            + "\t<indexLatch>0</indexLatch>\n"
            + "\t<isStopOnLatch>false</isStopOnLatch>\n"
            + "\t<homingTPS>10000000</homingTPS>\n"
            + "\t\n"
            + "\t<DHParameters>\n"
            + "\t\t<Delta>0</Delta>\n"
            + "\t\t<Theta>0.0</Theta>\n"
            + "\t\t<Radius>0</Radius>\n"
            + "\t\t<Alpha>0</Alpha>\n"
            + "\t</DHParameters>\n"
            + "\n"
            + "</link>\n"
            + "\n"
            + "<ZframeToRAS>\t\n"
            + "<x>0.0</x>\n"
            + "\t<y>0.0</y>\n"
            + "\t<z>0.0</z>\n"
            + "\t<rotw>1.0</rotw>\n"
            + "\t<rotx>0.0</rotx>\n"
            + "\t<roty>0.0</roty>\n"
            + "\t<rotz>0.0</rotz>\n"
            + "</ZframeToRAS>\n"
            + "\n"
            + "<baseToZframe>\n"
            + "\t<x>0.0</x>\n"
            + "\t<y>0.0</y>\n"
            + "\t<z>0.0</z>\n"
            + "\t<rotw>1.0</rotw>\n"
            + "\t<rotx>0.0</rotx>\n"
            + "\t<roty>0.0</roty>\n"
            + "\t<rotz>0.0</rotz>\n"
            + "</baseToZframe>\n"
            + "\n"
            + "</appendage>\n"
            + "\n"
            + "<ZframeToRAS>\n"
            + "\t<x>0.0</x>\n"
            + "\t<y>0.0</y>\n"
            + "\t<z>0.0</z>\n"
            + "\t<rotw>1.0</rotw>\n"
            + "\t<rotx>0.0</rotx>\n"
            + "\t<roty>0.0</roty>\n"
            + "\t<rotz>0.0</rotz>\n"
            + "</ZframeToRAS>\n"
            + "\n"
            + "<baseToZframe>\n"
            + "\t<x>0.0</x>\n"
            + "\t<y>0.0</y>\n"
            + "\t<z>0.0</z>\n"
            + "\t<rotw>1.0</rotw>\n"
            + "\t<rotx>0.0</rotx>\n"
            + "\t<roty>0.0</roty>\n"
            + "\t<rotz>0.0</rotz>\n"
            + "</baseToZframe>\n"
            + "\n"
            + "</mobilebase>\n"
            + "\n"
            + "</root>");
  }
}
