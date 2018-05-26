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
package com.neuronrobotics.DefaultDhSolver;

import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DefaultDhSolverTest {

  @Test
  void createADhSolver() {
    DhInverseSolver dhInverseSolver =
        (target, jointSpaceVector, chain) -> {
          final List<DHLink> links = chain.getLinks();

          final int linkNum = jointSpaceVector.length;
          final double[] inv = new double[linkNum];

          final double d = links.get(1).getD() - links.get(2).getD();
          final double r = links.get(0).getR();

          final double lengthXYPlaneVect =
              Math.sqrt(Math.pow(target.getX(), 2) + Math.pow(target.getY(), 2));
          final double angleXYPlaneVect = Math.asin(target.getY() / lengthXYPlaneVect);

          final double angleRectangleAdjustedXY = Math.asin(d / lengthXYPlaneVect);

          final double lengthRectangleAdjustedXY =
              lengthXYPlaneVect * Math.cos(angleRectangleAdjustedXY) - r;

          double orientation = angleXYPlaneVect - angleRectangleAdjustedXY;
          if (Math.abs(Math.toDegrees(orientation)) < 0.01) {
            orientation = 0;
          }
          double ySet = lengthRectangleAdjustedXY * Math.sin(orientation);
          double xSet = lengthRectangleAdjustedXY * Math.cos(orientation);

          double zSet = target.getZ() - links.get(0).getD();
          if (links.size() == 5) {
            final double tipAngulationSum =
                Math.toDegrees(
                    links.get(1).getTheta() + links.get(2).getTheta() + links.get(4).getTheta());

            if (tipAngulationSum == 90) {
              zSet += links.get(4).getD();
            } else {
              final double tipySet = links.get(4).getR() * Math.sin(orientation);
              final double tipxSet = links.get(4).getR() * Math.cos(orientation);

              xSet -= tipxSet;
              ySet -= tipySet;
            }
          }
          if (links.size() == 4) {
            final double tipAngleSum =
                Math.toDegrees(
                    links.get(1).getTheta() + links.get(2).getTheta() + links.get(3).getTheta());

            if (tipAngleSum == 90) zSet += links.get(3).getR();
            else {
              final double tipYSet = links.get(3).getR() * Math.sin(orientation);
              final double tipXSet = links.get(3).getR() * Math.cos(orientation);

              xSet -= tipXSet;
              ySet -= tipYSet;
            }
          }

          final double l1 = links.get(1).getR(); // First link length
          final double l2 = links.get(2).getR();

          final double hypotenuse = Math.sqrt(xSet * xSet + ySet * ySet + zSet * zSet);

          if (hypotenuse > l1 + l2 || hypotenuse < 0 || lengthRectangleAdjustedXY < 0) {
            throw new RuntimeException(
                "Hypotenuse too long: " + hypotenuse + " longer then " + l1 + l2);
          }

          // from https://www.mathsisfun.com/algebra/trig-solving-sss-triangles.html
          final double A =
              Math.acos(
                  (Math.pow(l1, 2) + Math.pow(hypotenuse, 2) - Math.pow(l2, 2))
                      / (2.0 * l1 * hypotenuse));
          final double B =
              Math.acos(
                  (Math.pow(hypotenuse, 2) + Math.pow(l2, 2) - Math.pow(l1, 2))
                      / (2.0 * l2 * hypotenuse));
          final double C = Math.PI - A - B; // Rule of triangles
          final double elevation = Math.asin(zSet / hypotenuse);

          inv[0] = Math.toDegrees(orientation);
          inv[1] = -Math.toDegrees((A + elevation + links.get(1).getTheta()));

          if ((int) links.get(1).getAlpha() == 180) {
            inv[2] =
                (Math.toDegrees(C))
                    - 180 // interior angle of the triangle, map to external angle
                    - Math.toDegrees(links.get(2).getTheta()); // offset for kinematics
          }
          if ((int) links.get(1).getAlpha() == 0) {
            inv[2] =
                -(Math.toDegrees(C))
                    + Math.toDegrees(links.get(2).getTheta()); // offset for kinematics
          }
          if (links.size() > 3) inv[3] = -(inv[1] + inv[2]); // keep it parallel

          // We know the wrist twist will always be 0 for this model
          if (links.size() > 4) inv[4] = inv[0]; // keep the tool orientation parallel from the base

          for (int i = 0; i < inv.length; i++) {
            if (Math.abs(inv[i]) < 0.01) {
              inv[i] = 0;
            }
          }

          int i = 3;
          if (links.size() > 3) i = 5;

          // copy over remaining links so they do not move
          for (; i < inv.length; i++) {
            inv[i] = jointSpaceVector[i];
          }

          return inv;
        };

    final DHChain chain = new DHChain(null);
    // DH params taken from
    // https://gist.github.com/madhephaestus/17f2da51abe25e043043946d91610ddd#file-jaguar-xml
    chain.addLink(new DHLink(4.19, 0.01, 11.45, -89.97));
    chain.addLink(new DHLink(0.0, 0.01, 38.1, 0.01));
    chain.addLink(new DHLink(0.0, 90.01, 38.1, 0.01));

    final double[] jointSpaceVector = {0, 0, 0};
    dhInverseSolver.inverseKinematics(new TransformNR(), jointSpaceVector, chain);
  }
}
