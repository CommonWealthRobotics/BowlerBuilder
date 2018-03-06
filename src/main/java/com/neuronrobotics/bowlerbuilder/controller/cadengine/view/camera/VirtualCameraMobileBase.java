/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.cadengine.view.camera;

import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class VirtualCameraMobileBase extends MobileBase { //NOPMD

  private IDriveEngine de = new IDriveEngineImplementation();
  private final ArrayList<VirtualCameraMobileBase> bases = new ArrayList<>();

  public VirtualCameraMobileBase(String text) throws Exception {
    //super(new FileInputStream(AssetFactory.loadFile("layout/flyingCamera.xml")));
    super(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8.name())));

    setWalkingDriveEngine(de);
    bases.add(this);
  }

  /**
   * Set a new drive engine. The walking drive engine for each base will also be set.
   *
   * @param de drive engine
   */
  public void setDriveEngine(IDriveEngine de) {
    this.de = de;
    for (VirtualCameraMobileBase base : bases) {
      base.setWalkingDriveEngine(getDriveEngine());
    }
  }

  public IDriveEngine getDriveEngine() {
    return de;
  }

  private static final class IDriveEngineImplementation implements IDriveEngine {

    double azOffset;
    double elOffset;
    double tlOffset;

    TransformNR pureTrans = new TransformNR();

    /**
     * Not used.
     */
    @Override
    public void DriveVelocityStraight(MobileBase source, double cmPerSecond) {
      //Not used
    }

    /**
     * Not used.
     */
    @Override
    public void DriveVelocityArc(MobileBase source, double degreesPerSecond, double cmRadius) {
      //Not used
    }

    /**
     * Move in an arc.
     *
     * @param source base to move
     * @param newPose transform to move on
     * @param seconds time to move over
     */
    @Override
    public void DriveArc(MobileBase source, TransformNR newPose, double seconds) {
      pureTrans.setX(newPose.getX());
      pureTrans.setY(newPose.getY());
      pureTrans.setZ(newPose.getZ());

      TransformNR global = source.getFiducialToGlobalTransform().times(pureTrans);
      global.setRotation(new RotationNR(
          tlOffset + (Math.toDegrees(
              newPose.getRotation().getRotationTilt() + global.getRotation().getRotationTilt())
              % 360),
          azOffset + (Math.toDegrees(newPose.getRotation().getRotationAzimuth()
              + global.getRotation().getRotationAzimuth()) % 360),
          elOffset + Math.toDegrees(newPose.getRotation().getRotationElevation()
              + global.getRotation().getRotationElevation())
      ));

      source.setGlobalToFiducialTransform(global);
    }
  }

}
