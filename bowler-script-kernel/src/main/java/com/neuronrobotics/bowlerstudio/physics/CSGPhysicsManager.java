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

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vertex;
import java.util.ArrayList;
import javafx.scene.transform.Affine;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class CSGPhysicsManager implements IPhysicsManager {

  private RigidBody fallRigidBody;
  private final Affine ballLocation = new Affine();
  ArrayList<CSG> baseCSG = null;
  private Transform updateTransform = new Transform();
  private IPhysicsUpdate updateManager = null;
  private PhysicsCore core;

  public CSGPhysicsManager(final ArrayList<CSG> baseCSG, final Vector3f start, final double mass, final PhysicsCore core) {
    this(
        baseCSG,
        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), start, 1.0f)),
        mass,
        true,
        core);
  }

  private CSG loadCSGToPoints(
      final CSG baseCSG, final boolean adjustCenter, final Transform pose,
      final ObjectArrayList<Vector3f> arg0) {
    CSG finalCSG = baseCSG;

    if (adjustCenter) {
      final double xcenter = baseCSG.getMaxX() / 2 + baseCSG.getMinX() / 2;
      final double ycenter = baseCSG.getMaxY() / 2 + baseCSG.getMinY() / 2;
      final double zcenter = baseCSG.getMaxZ() / 2 + baseCSG.getMinZ() / 2;

      final TransformNR poseToMove = TransformFactory.bulletToNr(pose);
      if (baseCSG.getMaxX() < 1 || baseCSG.getMinX() > -1) {
        finalCSG = finalCSG.movex(-xcenter);
        poseToMove.translateX(xcenter);
      }
      if (baseCSG.getMaxY() < 1 || baseCSG.getMinY() > -1) {
        finalCSG = finalCSG.movey(-ycenter);
        poseToMove.translateY(ycenter);
      }
      if (baseCSG.getMaxZ() < 1 || baseCSG.getMinZ() > -1) {
        finalCSG = finalCSG.movez(-zcenter);
        poseToMove.translateZ(zcenter);
      }
      TransformFactory.nrToBullet(poseToMove, pose);
    }

    for (final Polygon p : finalCSG.getPolygons()) {
      for (final Vertex v : p.vertices) {
        arg0.add(new Vector3f((float) v.getX(), (float) v.getY(), (float) v.getZ()));
      }
    }
    return finalCSG;
  }

  public CSGPhysicsManager(
      final ArrayList<CSG> baseCSG, final Transform pose, final double mass, final boolean adjustCenter, final PhysicsCore core) {
    this.setBaseCSG(baseCSG); // force a hull of the shape to simplify physics

    final ObjectArrayList<Vector3f> arg0 = new ObjectArrayList<>();
    for (int i = 0; i < baseCSG.size(); i++) {

      final CSG back = loadCSGToPoints(baseCSG.get(i), adjustCenter, pose, arg0);
      back.setManipulator(baseCSG.get(i).getManipulator());
      baseCSG.set(i, back);
    }
    final CollisionShape fallShape = new ConvexHullShape(arg0);
    setup(fallShape, pose, mass, core);
  }

  private void setup(
      final CollisionShape fallShape, final Transform pose, final double mass,
      final PhysicsCore core) {
    this.setCore(core);

    // setup the motion state for the ball
    System.out.println("Starting Object at " + TransformFactory.bulletToNr(pose));
    final DefaultMotionState fallMotionState = new DefaultMotionState(pose);
    // This we're going to give mass so it responds to gravity
    final Vector3f fallInertia = new Vector3f(0, 0, 0);
    fallShape.calculateLocalInertia((float) mass, fallInertia);
    final RigidBodyConstructionInfo fallRigidBodyCI =
        new RigidBodyConstructionInfo((float) mass, fallMotionState, fallShape, fallInertia);
    fallRigidBodyCI.additionalDamping = true;
    setFallRigidBody(new RigidBody(fallRigidBodyCI));
    // update(40);
  }

  public void update(final float timeStep) {
    fallRigidBody.getMotionState().getWorldTransform(updateTransform);
    if (getUpdateManager() != null) {
      getUpdateManager().update(timeStep);
    }
  }

  public RigidBody getFallRigidBody() {
    return fallRigidBody;
  }

  private void setFallRigidBody(final RigidBody fallRigidBody) {

    this.fallRigidBody = fallRigidBody;
  }

  public ArrayList<CSG> getBaseCSG() {
    return baseCSG;
  }

  private void setBaseCSG(final ArrayList<CSG> baseCSG) {
    for (final CSG c : baseCSG) {
      c.setManipulator(getRigidBodyLocation());
    }
    this.baseCSG = baseCSG;
  }

  public Transform getUpdateTransform() {
    return updateTransform;
  }

  public Affine getRigidBodyLocation() {
    return ballLocation;
  }

  IPhysicsUpdate getUpdateManager() {
    return updateManager;
  }

  public void setUpdateManager(final IPhysicsUpdate updateManager) {
    this.updateManager = updateManager;
  }

  PhysicsCore getCore() {
    return core;
  }

  private void setCore(final PhysicsCore core) {
    this.core = core;
  }
}
