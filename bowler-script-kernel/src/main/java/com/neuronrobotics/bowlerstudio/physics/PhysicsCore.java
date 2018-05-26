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

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import javafx.application.Platform;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class PhysicsCore {

  private BroadphaseInterface broadphase = new DbvtBroadphase();
  private DefaultCollisionConfiguration collisionConfiguration =
      new DefaultCollisionConfiguration();
  private CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

  private SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

  private DiscreteDynamicsWorld dynamicsWorld =
      new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
  // setup our collision shapes
  private CollisionShape groundShape = null;

  private ArrayList<IPhysicsManager> objects = new ArrayList<>();
  private RigidBody groundRigidBody;

  private boolean runEngine = false;
  private int msTime = 16;

  private Thread physicsThread = null;
  private int simulationSubSteps = 5;
  private float lin_damping;
  private float ang_damping;
  private float linearSleepThreshhold;
  private float angularSleepThreshhold;
  private float deactivationTime;

  public PhysicsCore() {
    // set the gravity of our world
    getDynamicsWorld()
        .setGravity(
            new Vector3f(0, 0, (float) -98 * MobileBasePhysicsManager.PhysicsGravityScalar));

    setGroundShape(new StaticPlaneShape(new Vector3f(0, 0, 10), 1));
  }

  public BroadphaseInterface getBroadphase() {
    return broadphase;
  }

  public void setBroadphase(final BroadphaseInterface broadphase) {
    this.broadphase = broadphase;
  }

  public DefaultCollisionConfiguration getCollisionConfiguration() {
    return collisionConfiguration;
  }

  public void setCollisionConfiguration(final DefaultCollisionConfiguration collisionConfiguration) {
    this.collisionConfiguration = collisionConfiguration;
  }

  public CollisionDispatcher getDispatcher() {
    return dispatcher;
  }

  public void setDispatcher(final CollisionDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public SequentialImpulseConstraintSolver getSolver() {
    return solver;
  }

  public void setSolver(final SequentialImpulseConstraintSolver solver) {
    this.solver = solver;
  }

  public DiscreteDynamicsWorld getDynamicsWorld() {
    return dynamicsWorld;
  }

  public void setDynamicsWorld(final DiscreteDynamicsWorld dynamicsWorld) {
    this.dynamicsWorld = dynamicsWorld;
  }

  public CollisionShape getGroundShape() {
    return groundShape;
  }

  private void setGroundShape(final CollisionShape cs) {
    if (groundRigidBody != null) {
      getDynamicsWorld().removeRigidBody(groundRigidBody); // add our
      // ground to
      // the
    }
    this.groundShape = cs;
    // setup the motion state
    final DefaultMotionState groundMotionState =
        new DefaultMotionState(
            new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0), 1.0f)));

    final RigidBodyConstructionInfo groundRigidBodyCI =
        new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
    groundRigidBody = new RigidBody(groundRigidBodyCI);
    dynamicsWorld.addRigidBody(groundRigidBody); // add our ground to the
  }

  private ArrayList<IPhysicsManager> getPhysicsObjects() {
    return objects;
  }

  public void setDamping(final float lin_damping, final float ang_damping) {
    this.lin_damping = (lin_damping);
    this.ang_damping = (ang_damping);
    for (final IPhysicsManager m : getPhysicsObjects()) {
      m.getFallRigidBody().setDamping(lin_damping, ang_damping);
    }
  }

  public void setSleepingThresholds(final float linearSleepThreshhold, final float angularSleepThreshhold) {
    this.linearSleepThreshhold = (linearSleepThreshhold);
    this.angularSleepThreshhold = (angularSleepThreshhold);
    for (final IPhysicsManager m : getPhysicsObjects()) {
      m.getFallRigidBody().setSleepingThresholds(linearSleepThreshhold, angularSleepThreshhold);
    }
  }

  public void setDeactivationTime(final float deactivationTime) {
    this.deactivationTime = deactivationTime;
    for (final IPhysicsManager m : getPhysicsObjects()) {
      m.getFallRigidBody().setDeactivationTime(deactivationTime);
    }
  }

  public void setObjects(final ArrayList<IPhysicsManager> objects) {
    this.objects = objects;
  }

  public void startPhysicsThread(final int ms) {
    msTime = ms;
    if (physicsThread == null) {
      runEngine = true;
      physicsThread =
          new Thread(
              () -> {
                while (runEngine) {
                  try {
                    final long start = System.currentTimeMillis();
                    stepMs(msTime);
                    final long took = (System.currentTimeMillis() - start);
                    if (took < msTime) {
                      ThreadUtil.wait((int) (msTime - took));
                    } else {
                      System.out.println("Real time physics broken: " + took);
                    }
                  } catch (final Exception E) {
                    E.printStackTrace();
                  }
                }
              });
      physicsThread.start();
    }
  }

  public ArrayList<CSG> getCsgFromEngine() {
    final ArrayList<CSG> csg = new ArrayList<>();
    for (final IPhysicsManager o : getPhysicsObjects()) {
      for (final CSG c : o.getBaseCSG()) {
        csg.add(c);
      }
    }
    return csg;
  }

  public void stopPhysicsThread() {
    physicsThread = null;
    runEngine = false;
  }

  public void step(final float timeStep) {
    final long startTime = System.currentTimeMillis();

    getDynamicsWorld().stepSimulation(timeStep, getSimulationSubSteps());
    if ((((float) (System.currentTimeMillis() - startTime)) / 1000.0f) > timeStep) {
      // System.out.println(" Compute took too long "+timeStep);
    }
    for (final IPhysicsManager o : getPhysicsObjects()) {
      o.update(timeStep);
    }

    Platform.runLater(
        () -> {
          for (final IPhysicsManager o : getPhysicsObjects()) {
            try {
              TransformFactory.bulletToAffine(o.getRigidBodyLocation(), o.getUpdateTransform());
            } catch (final Exception e) {

            }
          }
        });
  }

  public void stepMs(final double timeStep) {
    step((float) (timeStep / 1000.0));
  }

  public void add(final IPhysicsManager manager) {
    if (!getPhysicsObjects().contains(manager)) {
      getPhysicsObjects().add(manager);
      if (!WheelCSGPhysicsManager.class.isInstance(manager)
          && !VehicleCSGPhysicsManager.class.isInstance(manager)) {
        getDynamicsWorld().addRigidBody(manager.getFallRigidBody());
      }
      if (HingeCSGPhysicsManager.class.isInstance(manager)) {
        if (((HingeCSGPhysicsManager) manager).getConstraint() != null) {
          getDynamicsWorld()
              .addConstraint(((HingeCSGPhysicsManager) manager).getConstraint(), true);
        }
      }
      if (VehicleCSGPhysicsManager.class.isInstance(manager)) {
        getDynamicsWorld().addVehicle(((VehicleCSGPhysicsManager) manager).getVehicle());
      }
    }
  }

  public void remove(final IPhysicsManager manager) {
    if (getPhysicsObjects().contains(manager)) {
      getPhysicsObjects().remove(manager);
      if (!WheelCSGPhysicsManager.class.isInstance(manager)
          && !VehicleCSGPhysicsManager.class.isInstance(manager)) {

        getDynamicsWorld().removeRigidBody(manager.getFallRigidBody());
      }
      if (HingeCSGPhysicsManager.class.isInstance(manager)) {
        if (((HingeCSGPhysicsManager) manager).getConstraint() != null) {
          getDynamicsWorld().removeConstraint(((HingeCSGPhysicsManager) manager).getConstraint());
        }
      }
      if (VehicleCSGPhysicsManager.class.isInstance(manager)) {
        getDynamicsWorld().removeVehicle(((VehicleCSGPhysicsManager) manager).getVehicle());
      }
    }
  }

  public void clear() {
    stopPhysicsThread();
    ThreadUtil.wait(msTime * 2);
    for (final IPhysicsManager manager : getPhysicsObjects()) {
      if (!WheelCSGPhysicsManager.class.isInstance(manager)
          && !VehicleCSGPhysicsManager.class.isInstance(manager)) {
        getDynamicsWorld().removeRigidBody(manager.getFallRigidBody());
      }
      if (HingeCSGPhysicsManager.class.isInstance(manager)) {
        if (((HingeCSGPhysicsManager) manager).getConstraint() != null) {
          getDynamicsWorld().removeConstraint(((HingeCSGPhysicsManager) manager).getConstraint());
        }
      }
      if (VehicleCSGPhysicsManager.class.isInstance(manager)) {
        getDynamicsWorld().removeVehicle(((VehicleCSGPhysicsManager) manager).getVehicle());
      }
    }
    getPhysicsObjects().clear();
  }

  private int getSimulationSubSteps() {
    return simulationSubSteps;
  }

  public float getDeactivationTime() {
    return deactivationTime;
  }

  public void setSimulationSubSteps(final int simpulationSubSteps) {
    this.simulationSubSteps = simpulationSubSteps;
  }

  public float getLin_damping() {
    return lin_damping;
  }

  public float getAng_damping() {
    return ang_damping;
  }

  public float getLinearSleepThreshhold() {
    return linearSleepThreshhold;
  }

  public float getAngularSleepThreshhold() {
    return angularSleepThreshhold;
  }
}
