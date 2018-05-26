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
package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.util.FileWatchDeviceWrapper;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.IDriveEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;

public class MobileBaseLoader {

  private static HashMap<MobileBase, MobileBaseLoader> map = new HashMap<>();
  private MobileBase base;
  private IDriveEngine defaultDriveEngine;

  private MobileBaseLoader(final MobileBase base) {
    this.setBase(base);

    setDefaultWalkingEngine(base);
  }

  public void setGitDhEngine(final String gitsId, final String file, final DHParameterKinematics dh) {
    dh.setGitDhEngine(new String[] {gitsId, file});

    setDefaultDhParameterKinematics(dh);
  }

  private File setDefaultDhParameterKinematics(final DHParameterKinematics device) {
    File code = null;
    try {
      code = ScriptingEngine.fileFromGit(device.getGitDhEngine()[0], device.getGitDhEngine()[1]);
      final DhInverseSolver defaultDHSolver =
          (DhInverseSolver) ScriptingEngine.inlineFileScriptRun(code, null);

      final File c = code;
      FileWatchDeviceWrapper.watch(
          device,
          code,
          (fileThatChanged, event) -> {
            try {
              System.out.println("D-H Solver changed, updating " + device.getScriptingName());
              final DhInverseSolver d = (DhInverseSolver) ScriptingEngine.inlineFileScriptRun(c, null);
              device.setInverseSolver(d);
            } catch (final Exception ex) {
              MobileBaseCadManager.get(base).getUi().highlightException(c, ex);
            }
          });

      device.setInverseSolver(defaultDHSolver);
      return code;
    } catch (final Exception e1) {
      MobileBaseCadManager.get(base).getUi().highlightException(code, e1);
    }
    return null;
  }

  private void setDefaultWalkingEngine(final MobileBase device) {
    if (defaultDriveEngine == null) {
      setGitWalkingEngine(device.getGitWalkingEngine()[0], device.getGitWalkingEngine()[1], device);
    }
    for (final DHParameterKinematics dh : device.getAllDHChains()) {
      setDefaultDhParameterKinematics(dh);
    }
  }

  private void setGitWalkingEngine(final String git, final String file, final MobileBase device) {

    device.setGitWalkingEngine(new String[] {git, file});
    File code = null;
    try {
      code = ScriptingEngine.fileFromGit(git, file);
    } catch (final IOException e) {
      MobileBaseCadManager.get(base).getUi().highlightException(code, e);
    }

    final File c = code;
    FileWatchDeviceWrapper.watch(
        device,
        code,
        (fileThatChanged, event) -> {
          try {

            defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineFileScriptRun(c, null);
            device.setWalkingDriveEngine(defaultDriveEngine);
          } catch (final Exception ex) {
            MobileBaseCadManager.get(base).getUi().highlightException(c, ex);
          }
        });

    try {
      defaultDriveEngine = (IDriveEngine) ScriptingEngine.inlineFileScriptRun(c, null);
      device.setWalkingDriveEngine(defaultDriveEngine);
    } catch (final Exception ex) {
      MobileBaseCadManager.get(base).getUi().highlightException(c, ex);
    }
  }

  private static MobileBase initializeScripts(final MobileBase base) {
    if (map.get(base) == null) {
      map.put(base, new MobileBaseLoader(base));
    }

    return base;
  }

  public static MobileBase fromGit(final String id, final String file) throws Exception {
    final String xmlContent = ScriptingEngine.codeFromGit(id, file)[0];
    final MobileBase mb = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));

    mb.setGitSelfSource(new String[] {id, file});
    // ConnectionManager.addConnection(mb, mb.getScriptingName());
    return initializeScripts(mb);
  }

  public static MobileBaseLoader get(final MobileBase base) {

    return map.get(initializeScripts(base));
  }

  public MobileBase getBase() {
    return base;
  }

  private void setBase(final MobileBase base) {
    this.base = base;
  }
}
