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
package com.neuronrobotics;

import static org.junit.jupiter.api.Assertions.fail;

import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class MobileBaseLoading {
  int numCSG = 0;

  @Test
  public void test() throws Exception {

    ScriptingEngine.setupAnyonmous();
    numCSG = 0;
    IMobileBaseUI mobileBaseUI =
        new IMobileBaseUI() {
          @Override
          public void setAllCSG(Collection<CSG> collection, File file) {
            System.out.println("Setting CSG's # " + collection.size());
            numCSG = collection.size();
          }

          @Override
          public void addCSG(Collection<CSG> collection, File file) {
            System.out.println("Adding CSG's # " + collection.size());
          }

          @Override
          public void highlightException(File file, Exception e) {
            e.printStackTrace();
            fail();
          }

          @Override
          public Set<CSG> getVisibleCSGs() {
            return null;
          }

          @Override
          public void setSelectedCsg(Collection<CSG> collection) {}
        };

    CSG.setProgressMoniter((currentIndex, finalIndex, type, intermediateShape) -> {});

    String[] file = {"https://github.com/madhephaestus/SeriesElasticActuator.git", "seaArm.xml"};
    String xmlContent = ScriptingEngine.codeFromGit(file[0], file[1])[0];
    MobileBase mobileBase = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));
    mobileBase.setGitSelfSource(file);
    mobileBase.connect();
    MobileBaseCadManager mobileBaseCadManager = new MobileBaseCadManager(mobileBase, mobileBaseUI);
    // MobileBaseCadManager.get(mobileBase).getUi().
    DeviceManager.addConnection(mobileBase, mobileBase.getScriptingName());
    mobileBaseCadManager.generateCad();
    System.out.println("Waiting for cad to generate");
    ThreadUtil.wait(1000);
    while (MobileBaseCadManager.get(mobileBase).getProcesIndictor().get() < 1) {
      // System.out.println("Waiting: " +
      // MobileBaseCadManager.get(mobileBase).getProcesIndictor().get());
      ThreadUtil.wait(1000);
    }
    if (numCSG == 0) fail();
  }
}
