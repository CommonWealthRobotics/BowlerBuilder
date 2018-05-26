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

import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;

class PhysicsEngine {

  private static PhysicsCore mainEngine;

  public static void startPhysicsThread(final int ms) {
    get().startPhysicsThread(ms);
  }

  public static void stopPhysicsThread() {
    get().stopPhysicsThread();
  }

  public static void step(final float timeStep) {
    get().step(timeStep);
  }

  public static void stepMs(final double timeStep) {
    get().stepMs(timeStep);
  }

  public static void add(final IPhysicsManager manager) {
    get().add(manager);
  }

  public static void remove(final IPhysicsManager manager) {
    get().remove(manager);
  }

  public static void clear() {
    get().clear();
    mainEngine = null;
  }

  public static PhysicsCore get() {
    if (mainEngine == null) {
      try {
        mainEngine = new PhysicsCore();
      } catch (final Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return mainEngine;
  }

  public static ArrayList<CSG> getCsgFromEngine() {

    return mainEngine.getCsgFromEngine();
  }
}
