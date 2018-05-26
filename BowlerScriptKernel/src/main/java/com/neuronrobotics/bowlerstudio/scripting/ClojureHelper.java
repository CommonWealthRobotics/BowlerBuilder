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

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class containing static utility methods for Java to Clojure interop
 *
 * @author Mike
 *     https://github.com/mikera/clojure-utils/blob/master/src/main/java/mikera/cljutils/Clojure.java
 */
public class ClojureHelper implements IScriptingLanguage {

  public static Var REQUIRE = var("clojure.core", "require");
  public static Var META = var("clojure.core", "meta");
  public static Var EVAL = var("clojure.core", "eval");
  public static Var READ_STRING = var("clojure.core", "load-string");

  /**
   * Require a namespace by name, loading it if necessary.
   *
   * <p>Calls clojure.core/require
   */
  public static Object require(String nsName) {
    return REQUIRE.invoke(Symbol.intern(nsName));
  }

  public static Object readString(String s) {
    return READ_STRING.invoke(s);
  }

  /**
   * Looks up a var by name in the clojure.core namespace.
   *
   * <p>The var can subsequently be invoked if it is a function.
   */
  public static Var var(String varName) {
    return var("clojure.core", varName);
  }

  /**
   * Looks up a var by name in the given namespace.
   *
   * <p>The var can subsequently be invoked if it is a function.
   */
  public static Var var(String nsName, String varName) {
    return RT.var(nsName, varName);
  }

  /** Evaluates a String, which should contain valid Clojure code. */
  public static Object eval(String string) {
    return EVAL.invoke(readString(string));
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
    // System.out.println("Clojure returned of type="+ret.getClass()+" value="+ret);
    return null;
  }

  @Override
  public Object inlineScriptRun(String code, ArrayList<Object> args) {

    return ClojureHelper.eval(code);
  }

  @Override
  public String getShellType() {
    return "Clojure";
  }

  @Override
  public boolean getIsTextFile() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    // TODO Auto-generated method stub
    return new ArrayList<>(Arrays.asList("clj", "cljs", "cljc"));
  }
}
