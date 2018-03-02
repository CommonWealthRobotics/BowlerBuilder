package com.neuronrobotics.bowlerbuilder.view.cadengine.camera;

import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class VirtualCameraMobileBase extends MobileBase { //NOPMD

  private IDriveEngine driveEngine = new IDriveEngineImplementation();
  private final ArrayList<VirtualCameraMobileBase> bases = new ArrayList<>();

  public VirtualCameraMobileBase(final String text) throws Exception {
    //super(new FileInputStream(AssetFactory.loadFile("layout/flyingCamera.xml")));
    super(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8.name())));

    setWalkingDriveEngine(driveEngine);
    bases.add(this);
  }

  /**
   * Set a new drive engine. The walking drive engine for each base will also be set.
   *
   * @param driveEngine drive engine
   */
  public void setDriveEngine(final IDriveEngine driveEngine) {
    this.driveEngine = driveEngine;
    for (final VirtualCameraMobileBase base : bases) {
      base.setWalkingDriveEngine(getDriveEngine());
    }
  }

  public IDriveEngine getDriveEngine() {
    return driveEngine;
  }

  private static final class IDriveEngineImplementation implements IDriveEngine {

    double azOffset;
    double elOffset;
    double tlOffset;

    final TransformNR pureTrans = new TransformNR();

    /**
     * Not used.
     */
    @Override
    public void DriveVelocityStraight(final MobileBase source, final double cmPerSecond) {
      //Not used
    }

    /**
     * Not used.
     */
    @Override
    public void DriveVelocityArc(
        final MobileBase source, final double degreesPerSecond, final double cmRadius) {
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
    public void DriveArc(final MobileBase source, final TransformNR newPose, final double seconds) {
      pureTrans.setX(newPose.getX());
      pureTrans.setY(newPose.getY());
      pureTrans.setZ(newPose.getZ());

      final TransformNR global = source.getFiducialToGlobalTransform().times(pureTrans);
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
