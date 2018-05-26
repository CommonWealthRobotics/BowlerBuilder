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

import java.io.File;
import java.util.ArrayList;

/**
 * Adding additional language support to bowler studio THis interface is for adding new scripting
 * languages Add the new langauge in the Static declaration of ScriptingEngine or dynamically via:
 *
 * <p>ScriptingEngine.addScriptingLanguage(new IScriptingLanguage());
 *
 * @author hephaestus
 */
public interface IScriptingLanguage {

  /**
   * This interface is for adding additional language support.
   *
   * @param code file content of the code to be executed
   * @param args the incoming arguments as a list of objects
   * @return the objects returned form the code that ran
   */
  public abstract Object inlineScriptRun(File code, ArrayList<Object> args) throws Exception;

  /**
   * This interface is for adding additional language support.
   *
   * @param code the text content of the code to be executed
   * @param args the incoming arguments as a list of objects
   * @return the objects returned form the code that ran
   */
  public abstract Object inlineScriptRun(String code, ArrayList<Object> args) throws Exception;

  /** Returns the HashMap key for this language */
  public abstract String getShellType();

  /**
   * Returns the list of supported file extentions Convention is to provide just the leters that
   * make up the file extention
   */
  public abstract ArrayList<String> getFileExtenetion();

  /**
   * This function should return true is the filename provided is of a supported file extension.
   * This function may never be called if this language is only used internally.
   *
   * @param filename the filename of the file to be executed
   * @return true if the file extension is supported, false otherwise.
   */
  default boolean isSupportedFileExtenetion(String filename) {
    for (String s : getFileExtenetion()) {
      if (filename.toLowerCase().endsWith(s.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   * This function returns if this is a binary file or a text file
   *
   * @return true if the file is a text file.
   */
  public boolean getIsTextFile();
}
