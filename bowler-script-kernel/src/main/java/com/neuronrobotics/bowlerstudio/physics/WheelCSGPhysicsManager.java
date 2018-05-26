/*
 * Copyright 2015 Kevin Harrington
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.neuronrobotics.bowlerstudio.physics;

import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.IClosedLoopController;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;

// import com.neuronrobotics.bowlerstudio.BowlerStudio;

public class WheelCSGPhysicsManager extends CSGPhysicsManager {

  private IClosedLoopController controller = null;
  private double target = 0;
  private static float muscleStrength = (float) 1000;
  boolean flagBroken = false;
  private double velocity;
  private RaycastVehicle vehicle;
  private final int wheelIndex;

  public WheelCSGPhysicsManager(
      ArrayList<CSG> baseCSG,
      Transform pose,
      double mass,
      PhysicsCore c,
      RaycastVehicle v,
      int wheelIndex) {
    super(baseCSG, pose, mass, false, c);
    this.vehicle = v;
    this.wheelIndex = wheelIndex;
  }

  @Override
  public void update(float timeStep) {
    // cut out the falling body update
    if (getUpdateManager() != null) {
      try {
        getUpdateManager().update(timeStep);
      } catch (Exception e) {
        // BowlerStudio.printStackTrace(e);
        throw e;
      }
    }
    if (getController() != null) {
      velocity = getController().compute(getWheelInfo().rotation, getTarget(), timeStep);
    }
    vehicle.updateWheelTransform(getWheelIndex(), true);
    TransformNR trans =
        TransformFactory.bulletToNr(vehicle.getWheelInfo(getWheelIndex()).worldTransform);
    // copy in the current wheel location
    TransformFactory.nrToBullet(trans, getUpdateTransform());
  }

  public double getTarget() {
    return target;
  }

  public void setTarget(double target) {
    this.target = target;
  }

  public static float getMotorStrength() {
    return muscleStrength;
  }

  public static void setMuscleStrength(float ms) {
    muscleStrength = ms;
  }

  public void setMuscleStrength(double muscleStrength) {
    setMuscleStrength((float) muscleStrength);
  }

  public IClosedLoopController getController() {
    return controller;
  }

  public void setController(IClosedLoopController controller) {
    this.controller = controller;
  }

  public WheelInfo getWheelInfo() {
    return vehicle.getWheelInfo(getWheelIndex());
  }

  public int getWheelIndex() {
    return wheelIndex;
  }
}
