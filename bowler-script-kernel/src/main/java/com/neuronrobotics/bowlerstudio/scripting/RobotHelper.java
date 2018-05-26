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
package com.neuronrobotics.bowlerstudio.scripting;

import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;

public class RobotHelper implements IScriptingLanguage {

  @Override
  public Object inlineScriptRun(final File code, final ArrayList<Object> args) {
    final byte[] bytes;
    try {
      bytes = Files.readAllBytes(code.toPath());
      final String s = new String(bytes, "UTF-8");
      final MobileBase mb;
      try {
        mb = new MobileBase(IOUtils.toInputStream(s, "UTF-8"));

        mb.setGitSelfSource(ScriptingEngine.findGitTagFromFile(code));
        return mb;
      } catch (final IOException e) {
        e.printStackTrace();
        return null;
      }
    } catch (final IOException e1) {
      e1.printStackTrace();
    }

    return null;
  }

  @Override
  public Object inlineScriptRun(final String code, final ArrayList<Object> args) {

    MobileBase mb;
    try {
      mb = new MobileBase(IOUtils.toInputStream(code, "UTF-8"));
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }

    return mb;
  }

  @Override
  public String getShellType() {
    return "MobilBaseXML";
  }

  @Override
  public boolean getIsTextFile() {
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    return new ArrayList<>(Arrays.asList("xml"));
  }
}
