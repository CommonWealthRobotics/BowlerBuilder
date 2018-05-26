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

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import javafx.scene.control.Tab;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class JythonHelper implements IScriptingLanguage {

  PythonInterpreter interp;

  @Override
  public Object inlineScriptRun(String code, ArrayList<Object> args) {
    Properties props = new Properties();
    PythonInterpreter.initialize(System.getProperties(), props, new String[] {""});
    if (interp == null) {
      interp = new PythonInterpreter();

      interp.exec("import sys");
    }

    for (String pm : DeviceManager.listConnectedDevice(null)) {
      BowlerAbstractDevice bad = DeviceManager.getSpecificDevice(null, pm);
      // passing into the scipt
      try {
        interp.set(bad.getScriptingName(), Class.forName(bad.getClass().getName()).cast(bad));
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      System.err.println("Device " + bad.getScriptingName() + " is " + bad);
    }
    interp.set("args", args);
    interp.exec(code);
    ArrayList<Object> results = new ArrayList<>();

    PyObject localVariables = interp.getLocals();

    try {
      results.add(interp.get("csg", CSG.class));
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      results.add(interp.get("tab", Tab.class));
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      results.add(interp.get("device", BowlerAbstractDevice.class));
    } catch (Exception e) {
      e.printStackTrace();
    }

    Log.debug("Jython return = " + results);
    return results;
  }

  @Override
  public Object inlineScriptRun(File code, ArrayList<Object> args) {
    byte[] bytes;
    try {
      bytes = Files.readAllBytes(code.toPath());
      String s = new String(bytes, "UTF-8");
      return inlineScriptRun(s, args);
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return null;
  }

  @Override
  public String getShellType() {
    return "Jython";
  }

  @Override
  public boolean getIsTextFile() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    // TODO Auto-generated method stub
    return new ArrayList<>(Arrays.asList("py", "jy"));
  }
}
