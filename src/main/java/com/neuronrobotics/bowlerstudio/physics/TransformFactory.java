package com.neuronrobotics.bowlerstudio.physics;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4f;

import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import Jama.Matrix;
import javafx.scene.transform.Affine;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Transform objects.
 */
public class TransformFactory extends com.neuronrobotics.sdk.addons.kinematics.TransformFactory{
	
	public static void nrToBullet(TransformNR nr,com.bulletphysics.linearmath.Transform bullet){
		bullet.origin.set(
				(float)nr.getX(), 
				(float)nr.getY(), 
				(float)nr.getZ());
		bullet.setRotation(new Quat4f(
				(float)nr.getRotation().getRotationMatrix2QuaturnionX(),
				(float)nr.getRotation().getRotationMatrix2QuaturnionY(), 
				(float)nr.getRotation().getRotationMatrix2QuaturnionZ(), 
				(float)nr.getRotation().getRotationMatrix2QuaturnionW()));
	}
	
	public static TransformNR bulletToNr(com.bulletphysics.linearmath.Transform bullet){
		 Quat4f out= new Quat4f();
		bullet.getRotation(out);
		return new TransformNR(bullet.origin.x,
				bullet.origin.y,
				bullet.origin.z, out.w, out.x, out.y, out.z);
	}
	
	public static void bulletToAffine(Affine affine,com.bulletphysics.linearmath.Transform bullet){
		 Quat4f out= new Quat4f();
		bullet.getRotation(out);
		
		double w = out.w;
		double x=out.x;
		double y=out.y;
		double z=out.z;
		double norm = Math.sqrt(w * w + x * x + y * y + z * z);
		// we explicitly test norm against one here, saving a division
		// at the cost of a test and branch. Is it worth it?
		double s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;
		// compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
		// will be used 2-4 times each.
		double xs = x * s;
		double ys = y * s;
		double zs = z * s;
		double xx = x * xs;
		double xy = x * ys;
		double xz = x * zs;
		double xw = w * xs;
		double yy = y * ys;
		double yz = y * zs;
		double yw = w * ys;
		double zz = z * zs;
		double zw = w * zs;
		
		
		affine.setMxx(1 - (yy + zz));
		affine.setMxy((xy - zw));
		affine.setMxz(xz + yw);
		affine.setMyx(xy + zw);
		affine.setMyy(1 - (xx + zz));
		affine.setMyz(yz - xw);
		affine.setMzx(xz - yw);
		affine.setMzy(yz + xw);
		affine.setMzz( 1 - (xx + yy));
		affine.setTx(bullet.origin.x);
		affine.setTy(bullet.origin.y);
		affine.setTz(bullet.origin.z);
	}
	public static void affineToBullet(Affine affine,com.bulletphysics.linearmath.Transform bullet){
		TransformNR nr = affineToNr(affine);
		nrToBullet(nr,bullet);
	}
	public static eu.mihosoft.vrl.v3d.Transform  nrToCSG(TransformNR nr){
		Matrix vals =nr.getMatrixTransform();
		double [] elemenents = new double[]{ 
			vals.get(0, 0),
			vals.get(0, 1),
			vals.get(0, 2),
			vals.get(0, 3),
			
			vals.get(1, 0),
			vals.get(1, 1),
			vals.get(1, 2),
			vals.get(1, 3),
			
			vals.get(2, 0),
			vals.get(2, 1),
			vals.get(2, 2),
			vals.get(2, 3),
			
			vals.get(3, 0),
			vals.get(3, 1),
			vals.get(3, 2),
			vals.get(3, 3),
		};
		
		
		Matrix4d rotation=	new Matrix4d(elemenents);
		return new eu.mihosoft.vrl.v3d.Transform(rotation);
	}
	
	public static TransformNR csgToNR(eu.mihosoft.vrl.v3d.Transform csg){
		Matrix4d rotation = csg.getInternalMatrix();
		Matrix start= new TransformNR().getMatrixTransform();
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				start.set(i, j, rotation.getElement(i, j));
		return new TransformNR();
	}
	
}
