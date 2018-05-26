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

  private static Var REQUIRE = var("clojure.core", "require");
  public static Var META = var("clojure.core", "meta");
  private static Var EVAL = var("clojure.core", "eval");
  private static Var READ_STRING = var("clojure.core", "load-string");

  /**
   * Require a namespace by name, loading it if necessary.
   *
   * <p>Calls clojure.core/require
   */
  public static Object require(final String nsName) {
    return REQUIRE.invoke(Symbol.intern(nsName));
  }

  private static Object readString(final String s) {
    return READ_STRING.invoke(s);
  }

  /**
   * Looks up a var by name in the clojure.core namespace.
   *
   * <p>The var can subsequently be invoked if it is a function.
   */
  public static Var var(final String varName) {
    return var("clojure.core", varName);
  }

  /**
   * Looks up a var by name in the given namespace.
   *
   * <p>The var can subsequently be invoked if it is a function.
   */
  private static Var var(final String nsName, final String varName) {
    return RT.var(nsName, varName);
  }

  /** Evaluates a String, which should contain valid Clojure code. */
  private static Object eval(final String string) {
    return EVAL.invoke(readString(string));
  }

  @Override
  public Object inlineScriptRun(final File code, final ArrayList<Object> args) {
    final byte[] bytes;
    try {
      bytes = Files.readAllBytes(code.toPath());
      final String s = new String(bytes, "UTF-8");
      return inlineScriptRun(s, args);
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return null;
  }

  @Override
  public Object inlineScriptRun(final String code, final ArrayList<Object> args) {

    return ClojureHelper.eval(code);
  }

  @Override
  public String getShellType() {
    return "Clojure";
  }

  @Override
  public boolean getIsTextFile() {
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    // TODO Auto-generated method stub
    return new ArrayList<>(Arrays.asList("clj", "cljs", "cljc"));
  }
}
