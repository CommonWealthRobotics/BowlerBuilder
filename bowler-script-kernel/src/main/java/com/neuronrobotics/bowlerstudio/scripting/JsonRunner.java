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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class JsonRunner implements IScriptingLanguage {

  // Create the type, this tells GSON what data types to instantiate when parsing and saving the
  // json
  private static Type TT_mapStringString =
      new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType();
  // create the gson object, this is the parsing factory
  private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

  @Override
  public Object inlineScriptRun(final File code, final ArrayList<Object> args) throws Exception {
    String jsonString;
    InputStream inPut;
    inPut = FileUtils.openInputStream(code);
    jsonString = IOUtils.toString(inPut);
    return inlineScriptRun(jsonString, args);
  }

  @Override
  public Object inlineScriptRun(final String code, final ArrayList<Object> args) {
    // perform the GSON parse
    return gson.<HashMap<String, HashMap<String, Object>>>fromJson(code, TT_mapStringString);
  }

  @Override
  public String getShellType() {
    return "JSON";
  }

  @Override
  public boolean getIsTextFile() {
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    return new ArrayList<>(Arrays.asList("json"));
  }
}
