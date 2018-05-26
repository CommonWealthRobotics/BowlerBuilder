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

import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.linearmath.Transform;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;

// import com.neuronrobotics.bowlerstudio.BowlerStudio;

public class VehicleCSGPhysicsManager extends CSGPhysicsManager {

  ////////////////////////////////////////////////////////////////////////////

  private VehicleTuning tuning = new VehicleTuning();
  private VehicleRaycaster vehicleRayCaster;
  private RaycastVehicle vehicle;

  public VehicleCSGPhysicsManager(
      final ArrayList<CSG> baseCSG, final Transform pose, final double mass, final boolean adjustCenter, final PhysicsCore core) {
    super(baseCSG, pose, mass, adjustCenter, core);

    vehicleRayCaster = new DefaultVehicleRaycaster(core.getDynamicsWorld());
    setVehicle(new RaycastVehicle(getTuning(), getFallRigidBody(), vehicleRayCaster));
  }

  @Override
  public void update(final float timeStep) {
    getFallRigidBody().getMotionState().getWorldTransform(getUpdateTransform());
    if (getUpdateManager() != null) {
      try {
        getUpdateManager().update(timeStep);
      } catch (final Exception e) {
        // BowlerStudio.printStackTrace(e);
        throw e;
      }
    }
    vehicle.updateVehicle(timeStep);
  }

  public RaycastVehicle getVehicle() {
    return vehicle;
  }

  private void setVehicle(final RaycastVehicle vehicle) {
    this.vehicle = vehicle;
  }

  private VehicleTuning getTuning() {
    return tuning;
  }

  public void setTuning(final VehicleTuning tuning) {
    this.tuning = tuning;
  }
}
