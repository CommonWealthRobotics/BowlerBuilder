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

import org.junit.jupiter.api.Test;

public class JsonTester {

  @Test
  public void test() throws Exception {
    /*
    try{
      ScriptingEngine.setupAnyonmous();
      //ScriptingEngine.setAutoupdate(true);
    }catch (Exception ex){
      System.out.println("User not logged in, test can not run");
    }
    File f = ScriptingEngine
        .fileFromGit(
            "https://github.com/madhephaestus/BowlerStudioExampleRobots.git",// git repo, change this if you fork this demo
          "exampleRobots.json"// File from within the Git repo
        );
    System.out.println("File: "+f);
    HashMap<String,HashMap<String,Object>> map = (HashMap<String, HashMap<String, Object>>) ScriptingEngine
                                  .inlineFileScriptRun(f, null);
    for(String menuTitle:map.keySet()){
      HashMap<String,Object> script = map.get(menuTitle);
      System.out.println((String)script.get("scriptGit"));
      System.out.println((String)script.get("scriptFile"));						;

    }
    */
  }
}
