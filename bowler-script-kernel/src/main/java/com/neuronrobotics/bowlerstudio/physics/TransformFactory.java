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

import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javafx.scene.transform.Affine;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;

// TODO: Auto-generated Javadoc

/** A factory for creating Transform objects. */
@SuppressWarnings("restriction")
public class TransformFactory extends com.neuronrobotics.sdk.addons.kinematics.TransformFactory {

  public static void nrToBullet(TransformNR nr, com.bulletphysics.linearmath.Transform bullet) {
    bullet.origin.set((float) nr.getX(), (float) nr.getY(), (float) nr.getZ());
    bullet.setRotation(
        new Quat4f(
            (float) nr.getRotation().getRotationMatrix2QuaturnionX(),
            (float) nr.getRotation().getRotationMatrix2QuaturnionY(),
            (float) nr.getRotation().getRotationMatrix2QuaturnionZ(),
            (float) nr.getRotation().getRotationMatrix2QuaturnionW()));
  }

  public static TransformNR bulletToNr(com.bulletphysics.linearmath.Transform bullet) {
    Quat4f out = new Quat4f();
    bullet.getRotation(out);
    return new TransformNR(
        bullet.origin.x, bullet.origin.y, bullet.origin.z, out.w, out.x, out.y, out.z);
  }

  public static void bulletToAffine(Affine affine, com.bulletphysics.linearmath.Transform bullet) {

    // synchronized(out){
    double[][] vals = bulletToNr(bullet).getRotationMatrix().getRotationMatrix();
    // we explicitly test norm against one here, saving a division
    // at the cost of a test and branch. Is it worth it?
    // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
    // will be used 2-4 times each.
    affine.setMxx(vals[0][0]);
    affine.setMxy(vals[0][1]);
    affine.setMxz(vals[0][2]);
    affine.setMyx(vals[1][0]);
    affine.setMyy(vals[1][1]);
    affine.setMyz(vals[1][2]);
    affine.setMzx(vals[2][0]);
    affine.setMzy(vals[2][1]);
    affine.setMzz(vals[2][2]);
    // }
    affine.setTx(bullet.origin.x);
    affine.setTy(bullet.origin.y);
    affine.setTz(bullet.origin.z);
  }

  public static void affineToBullet(Affine affine, com.bulletphysics.linearmath.Transform bullet) {
    TransformNR nr = affineToNr(affine);
    nrToBullet(nr, bullet);
  }

  public static eu.mihosoft.vrl.v3d.Transform nrToCSG(TransformNR nr) {
    Quat4d q1 = new Quat4d();
    q1.w = nr.getRotation().getRotationMatrix2QuaturnionW();
    q1.x = nr.getRotation().getRotationMatrix2QuaturnionX();
    q1.y = nr.getRotation().getRotationMatrix2QuaturnionY();
    q1.z = nr.getRotation().getRotationMatrix2QuaturnionZ();
    Vector3d t1 = new Vector3d();
    t1.x = nr.getX();
    t1.y = nr.getY();
    t1.z = nr.getZ();
    double s = 1.0;

    Matrix4d rotation = new Matrix4d(q1, t1, s);
    return new eu.mihosoft.vrl.v3d.Transform(rotation);
  }

  public static TransformNR csgToNR(eu.mihosoft.vrl.v3d.Transform csg) {
    Matrix4d rotation = csg.getInternalMatrix();
    Quat4d q1 = new Quat4d();
    rotation.get(q1);
    Vector3d t1 = new Vector3d();
    rotation.get(t1);

    return new TransformNR(t1.x, t1.y, t1.z, new RotationNR(q1.w, q1.x, q1.y, q1.z));
  }
}