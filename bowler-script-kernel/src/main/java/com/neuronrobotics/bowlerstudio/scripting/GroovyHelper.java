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
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class GroovyHelper implements IScriptingLanguage, IScriptingLanguageDebugger {

  private Object inline(Object code, ArrayList<Object> args) throws Exception {
    CompilerConfiguration cc = new CompilerConfiguration();
    cc.addCompilationCustomizers(
        new ImportCustomizer()
            .addStarImports(ScriptingEngine.getImports())
            .addStaticStars(
                "com.neuronrobotics.sdk.util.ThreadUtil",
                "eu.mihosoft.vrl.v3d.Transform",
                "com.neuronrobotics.bowlerstudio.vitamins.Vitamins"));

    Binding binding = new Binding();
    for (String pm : DeviceManager.listConnectedDevice()) {
      BowlerAbstractDevice bad = DeviceManager.getSpecificDevice(null, pm);
      try {
        // groovy needs the objects cas to thier actual type befor
        // passing into the scipt

        binding.setVariable(
            bad.getScriptingName(), Class.forName(bad.getClass().getName()).cast(bad));
      } catch (Throwable e) {
        // throw e;
      }
      //			System.err.println("Device " + bad.getScriptingName() + " is "
      //					+ bad);
    }
    binding.setVariable("args", args);

    GroovyShell shell = new GroovyShell(GroovyHelper.class.getClassLoader(), binding, cc);
    // System.out.println(code + "\n\nStart\n\n");
    Script script;
    if (String.class.isInstance(code)) {
      script = shell.parse((String) code);
    } else if (File.class.isInstance(code)) {
      script = shell.parse((File) code);
    } else {
      return null;
    }
    return script.run();
  }

  @Override
  public String getShellType() {
    return "Groovy";
  }

  @Override
  public Object inlineScriptRun(File code, ArrayList<Object> args) throws Exception {
    return inline(code, args);
  }

  @Override
  public Object inlineScriptRun(String code, ArrayList<Object> args) throws Exception {
    return inline(code, args);
  }

  @Override
  public boolean getIsTextFile() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    // TODO Auto-generated method stub
    return new ArrayList<>(Arrays.asList("java", "groovy"));
  }

  @Override
  public IDebugScriptRunner compileDebug(File f) {
    // TODO Auto-generated method stub
    return new IDebugScriptRunner() {

      @Override
      public String[] step() {
        // TODO Auto-generated method stub
        return new String[] {"fileame.groovy", "345"};
      }
    };
  }
}
