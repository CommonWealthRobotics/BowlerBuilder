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

import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.common.IClosedLoopController;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import javafx.scene.paint.Color;

public class HingeCSGPhysicsManager extends CSGPhysicsManager {

  private HingeConstraint constraint = null;
  private IClosedLoopController controller = null;
  private double target = 0;
  private static float muscleStrength = (float) 1000;
  private boolean flagBroken = false;
  private double velocity;

  public HingeCSGPhysicsManager(
      final ArrayList<CSG> baseCSG, final Transform pose, final double mass, final PhysicsCore c) {
    super(baseCSG, pose, mass, false, c);
  }

  @Override
  public void update(final float timeStep) {
    super.update(timeStep);
    if (constraint != null && getController() != null && !flagBroken) {
      velocity = getController().compute(constraint.getHingeAngle(), getTarget(), timeStep);
      constraint.enableAngularMotor(true, (float) velocity, getMuscleStrength());
      if (constraint.getAppliedImpulse() > getMuscleStrength()) {
        for (final CSG c1 : baseCSG) {
          c1.setColor(Color.WHITE);
        }
        flagBroken = true;
        getCore().remove(this);
        setConstraint(null);
        getCore().add(this);
        System.out.println(
            "ERROR Link Broken, Strength: "
                + getMuscleStrength()
                + " applied impluse "
                + constraint.getAppliedImpulse());
      }
    } else if (constraint != null && flagBroken) {
      constraint.enableAngularMotor(false, 0, 0);
    }
  }

  public HingeConstraint getConstraint() {
    return constraint;
  }

  public void setConstraint(final HingeConstraint constraint) {
    this.constraint = constraint;
  }

  private double getTarget() {
    return target;
  }

  public void setTarget(final double target) {
    this.target = target;
  }

  private static float getMuscleStrength() {
    return muscleStrength;
  }

  public static void setMuscleStrength(final float ms) {
    muscleStrength = ms;
  }

  public void setMuscleStrength(final double muscleStrength) {
    setMuscleStrength((float) muscleStrength);
  }

  private IClosedLoopController getController() {
    return controller;
  }

  public void setController(final IClosedLoopController controller) {
    this.controller = controller;
  }
}
