package com.neuronrobotics.bowlerbuilder.controller;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.io.FileInputStream;
import java.util.ArrayList;

public class VirtualCameraMobileBase extends MobileBase {

  private static IDriveEngine de = new IDriveEngineImplementation();
  private static ArrayList<VirtualCameraMobileBase> bases = new ArrayList<>();

  public VirtualCameraMobileBase() throws Exception {
    super(new FileInputStream(AssetFactory.loadFile("layout/flyingCamera.xml")));

    setWalkingDriveEngine(getDriveEngine());
    bases.add(this);
  }

  public static IDriveEngine getDriveEngine() {
    return de;
  }

  public static void setDriveEngine(IDriveEngine de) {
    VirtualCameraMobileBase.de = de;
    for (VirtualCameraMobileBase base : bases) {
      base.setWalkingDriveEngine(getDriveEngine());
    }
  }

  private final static class IDriveEngineImplementation implements IDriveEngine {
    double azOffset = 0;
    double elOffset = 0;
    double tlOffset = 0;

    TransformNR pureTrans = new TransformNR();

    @Override
    public void DriveVelocityStraight(MobileBase source, double cmPerSecond) {
    }

    @Override
    public void DriveVelocityArc(MobileBase source, double degreesPerSecond, double cmRadius) {
    }

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
