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

import Jama.Matrix;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.imu.IMU;
import com.neuronrobotics.sdk.addons.kinematics.imu.IMUUpdate;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.IClosedLoopController;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

class MobileBasePhysicsManager {

  public static final float PhysicsGravityScalar = 6;
  private HashMap<LinkConfiguration, ArrayList<CSG>> simplecad;
  private float lift = 20;
  private ArrayList<ILinkListener> linkListeners = new ArrayList<>();
  public static final float LIFT_EPS = (float) Math.toRadians(0.1);

  private IPhysicsUpdate getUpdater(final RigidBody body, final IMU base) {
    return new IPhysicsUpdate() {
      Vector3f oldavelocity = new Vector3f(0f, 0f, 0f);
      Vector3f oldvelocity = new Vector3f(0f, 0f, 0f);
      Vector3f gravity = new Vector3f();
      private Quat4f orentation = new Quat4f();
      Transform gravTrans = new Transform();
      Transform orentTrans = new Transform();
      Vector3f avelocity = new Vector3f();
      Vector3f velocity = new Vector3f();

      @Override
      public void update(final float timeStep) {

        body.getAngularVelocity(avelocity);
        body.getLinearVelocity(velocity);

        body.getGravity(gravity);
        body.getOrientation(orentation);

        TransformFactory.nrToBullet(
            new TransformNR(gravity.x, gravity.y, gravity.z, new RotationNR()), gravTrans);
        TransformFactory.nrToBullet(
            new TransformNR(0, 0, 0, orentation.w, orentation.x, orentation.y, orentation.z),
            orentTrans);
        orentTrans.inverse();
        orentTrans.mul(gravTrans);

        // A=DeltaV / DeltaT
        final Double rotxAcceleration = (double) ((oldavelocity.x - avelocity.x) / timeStep);
        final Double rotyAcceleration = (double) ((oldavelocity.y - avelocity.y) / timeStep);
        final Double rotzAcceleration = (double) ((oldavelocity.z - avelocity.z) / timeStep);
        final Double xAcceleration =
            (double) (((oldvelocity.x - velocity.x) / timeStep) / PhysicsGravityScalar)
                + (orentTrans.origin.x / PhysicsGravityScalar);
        final Double yAcceleration =
            (double) (((oldvelocity.y - velocity.y) / timeStep) / PhysicsGravityScalar)
                + (orentTrans.origin.y / PhysicsGravityScalar);
        final Double zAcceleration =
            (double) (((oldvelocity.z - velocity.z) / timeStep) / PhysicsGravityScalar)
                + (orentTrans.origin.z / PhysicsGravityScalar);
        // tell the virtual IMU the system updated
        base.setVirtualState(
            new IMUUpdate(
                xAcceleration,
                yAcceleration,
                zAcceleration,
                rotxAcceleration,
                rotyAcceleration,
                rotzAcceleration));
        // update the old variables
        oldavelocity.set(avelocity);
        oldvelocity.set(velocity);
      }
    };
  }

  public MobileBasePhysicsManager(
      final MobileBase base,
      final ArrayList<CSG> baseCad,
      final HashMap<LinkConfiguration, ArrayList<CSG>> simplecad) {
    this(base, baseCad, simplecad, PhysicsEngine.get());
  }

  private MobileBasePhysicsManager(
      final MobileBase base,
      final ArrayList<CSG> baseCad,
      final HashMap<LinkConfiguration, ArrayList<CSG>> simplecad,
      final PhysicsCore core) {
    this.simplecad = simplecad;
    double minz = 0;
    for (final DHParameterKinematics dh : base.getAllDHChains()) {
      if (dh.getCurrentTaskSpaceTransform().getZ() < minz) {
        minz = dh.getCurrentTaskSpaceTransform().getZ();
      }
    }
    for (final CSG c : baseCad) {
      if (c.getMinZ() < minz) {
        minz = c.getMinZ();
      }
    }

    // System.out.println("Minimum z = "+minz);
    final Transform start = new Transform();
    base.setFiducialToGlobalTransform(new TransformNR());
    // TransformNR globe= base.getFiducialToGlobalTransform();

    TransformFactory.nrToBullet(base.getFiducialToGlobalTransform(), start);
    start.origin.z = (float) (start.origin.z - minz + lift);
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            TransformFactory.bulletToAffine(baseCad.get(0).getManipulator(), start);
          }
        });
    final CSGPhysicsManager baseManager =
        new CSGPhysicsManager(baseCad, start, base.getMassKg(), false, core);
    final RigidBody body = baseManager.getFallRigidBody();
    baseManager.setUpdateManager(getUpdater(body, base.getImu()));

    core.getDynamicsWorld().setGravity(new Vector3f(0, 0, (float) -98 * PhysicsGravityScalar));
    core.add(baseManager);
    for (int j = 0; j < base.getAllDHChains().size(); j++) {
      final DHParameterKinematics dh = base.getAllDHChains().get(j);
      RigidBody lastLink = body;
      Matrix previousStep = null;
      ArrayList<TransformNR> cached = dh.getDhChain().getCachedChain();
      for (int i = 0; i < dh.getNumberOfLinks(); i++) {
        // Hardware to engineering units configuration
        final LinkConfiguration conf = dh.getLinkConfiguration(i);
        // DH parameters
        final DHLink l = dh.getDhChain().getLinks().get(i);
        final ArrayList<CSG> thisLinkCad = simplecad.get(conf);
        if (thisLinkCad != null && thisLinkCad.size() > 0) {
          boolean flagAlpha = false;
          boolean flagTheta = false;
          final double jogAmount = 0.001;
          // Check for singularities and just jog it off the
          // singularity.
          if (Math.toDegrees(l.getAlpha()) % 90 < jogAmount) {
            l.setAlpha(l.getAlpha() + Math.toRadians(jogAmount));
            cached = dh.getDhChain().getCachedChain();
            flagAlpha = true;
          }
          if (Math.toDegrees(l.getTheta()) % 90 < jogAmount) {
            l.setTheta(l.getTheta() + Math.toRadians(jogAmount));
            cached = dh.getDhChain().getCachedChain();
            flagTheta = true;
          }
          // use the DH parameters to calculate the offset of the link
          // at 0 degrees
          final Matrix step;
          if (conf.isPrismatic()) {
            step = l.DhStepInversePrismatic(0);
          } else {
            step = l.DhStepInverseRotory(Math.toRadians(0));
          }
          // correct jog for singularity.

          if (flagAlpha) {
            l.setAlpha(l.getAlpha() - Math.toRadians(jogAmount));
          }
          if (flagTheta) {
            l.setTheta(l.getTheta() - Math.toRadians(jogAmount));
          }

          // Engineering units to kinematics link (limits and hardware
          // type abstraction)
          final AbstractLink abstractLink = dh.getAbstractLink(i);

          // Transform used by the UI to render the location of the
          // object
          final Affine manipulator =
              new Affine(); // make a new affine for the physics engine to service. the manipulaters
          // in the CSG will not conflict for resources here
          // The DH chain calculated the starting location of the link
          // in its current configuration
          final TransformNR localLink = cached.get(i);
          // Lift it in the air so nothing is below the ground to
          // start.
          localLink.translateZ(lift);
          // Bullet engine transform object
          final Transform linkLoc = new Transform();
          TransformFactory.nrToBullet(localLink, linkLoc);
          linkLoc.origin.z = (float) (linkLoc.origin.z - minz + lift);

          // Set the manipulator to the location from the kinematics,
          // needs to be in UI thread to touch manipulator
          Platform.runLater(
              new Runnable() {
                @Override
                public void run() {
                  TransformFactory.nrToAffine(localLink, manipulator);
                }
              });
          ThreadUtil.wait(16);

          final double mass = conf.getMassKg();
          final ArrayList<CSG> outCad = new ArrayList<>();
          for (int x = 0; x < thisLinkCad.size(); x++) {
            final Color color = thisLinkCad.get(x).getColor();
            outCad.add(
                thisLinkCad
                    .get(x)
                    .transformed(TransformFactory.nrToCSG(new TransformNR(step).inverse())));
            outCad.get(x).setManipulator(manipulator);
            outCad.get(x).setColor(color);
          }
          // Build a hinge based on the link and mass
          final HingeCSGPhysicsManager hingePhysicsManager =
              new HingeCSGPhysicsManager(outCad, linkLoc, mass, core);
          HingeCSGPhysicsManager.setMuscleStrength(1000000);

          final RigidBody linkSection = hingePhysicsManager.getFallRigidBody();

          hingePhysicsManager.setUpdateManager(getUpdater(linkSection, abstractLink.getImu()));
          // // Setup some damping on the m_bodies
          linkSection.setDamping(0.5f, 08.5f);
          linkSection.setDeactivationTime(0.8f);
          linkSection.setSleepingThresholds(1.6f, 2.5f);

          final HingeConstraint joint6DOF;
          final Transform localA = new Transform();
          final Transform localB = new Transform();
          localA.setIdentity();
          localB.setIdentity();

          // set up the center of mass offset from the centroid of the
          // links
          if (i == 0) {

            TransformFactory.nrToBullet(dh.forwardOffset(new TransformNR()), localA);
          } else {
            TransformFactory.nrToBullet(new TransformNR(previousStep.inverse()), localA);
          }
          // set the link constraint based on DH parameters
          TransformFactory.nrToBullet(new TransformNR(), localB);
          previousStep = step;
          // build the hinge constraint
          joint6DOF = new HingeConstraint(lastLink, linkSection, localA, localB);
          joint6DOF.setLimit(
              -(float) Math.toRadians(abstractLink.getMinEngineeringUnits()),
              -(float) Math.toRadians(abstractLink.getMaxEngineeringUnits()));

          lastLink = linkSection;

          hingePhysicsManager.setConstraint(joint6DOF);

          if (!conf.isPassive()) {
            final ILinkListener ll =
                new ILinkListener() {
                  @Override
                  public void onLinkPositionUpdate(
                      final AbstractLink source, final double engineeringUnitsValue) {
                    // System.out.println("
                    // value="+engineeringUnitsValue);
                    hingePhysicsManager.setTarget(Math.toRadians(-engineeringUnitsValue));

                    //                                     joint6DOF.setLimit( (float)
                    //                                     (Math.toRadians(-engineeringUnitsValue )-
                    //                                     LIFT_EPS),
                    //                                     (float)
                    // (Math.toRadians(-engineeringUnitsValue )+
                    //                                     LIFT_EPS));
                  }

                  @Override
                  public void onLinkLimit(final AbstractLink source, final PIDLimitEvent event) {
                    // println event
                  }
                };
            hingePhysicsManager.setController(
                new IClosedLoopController() {

                  @Override
                  public double compute(final double currentState, final double target, final double seconds) {
                    final double error = target - currentState;
                    return (error / seconds) * (seconds * 10);
                  }
                });
            abstractLink.addLinkListener(ll);
            linkListeners.add(ll);
          }

          abstractLink.getCurrentPosition();
          core.add(hingePhysicsManager);
        }
      }
    }
  }
}
