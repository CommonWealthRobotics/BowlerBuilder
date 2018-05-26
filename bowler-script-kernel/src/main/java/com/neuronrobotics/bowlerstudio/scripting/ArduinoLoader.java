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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ArduinoLoader implements IScriptingLanguage {

  private static String ARDUINO = "arduino";

  private HashMap<String, HashMap<String, Object>> database;

  private static String defaultPort = null;
  private static String defaultBoard = null;
  private static boolean loadedBowler = false;

  @SuppressWarnings("unchecked")
  @Override
  public Object inlineScriptRun(final File code, ArrayList<Object> args) throws Exception {
    if (args == null) {
      args = new ArrayList<>();
    }
    if (database == null) {
      database =
          (HashMap<String, HashMap<String, Object>>)
              ScriptingEngine.gitScriptRun(
                  "https://github.com/madhephaestus/Arduino-Boards-JSON.git", "boards.json", null);
    }
    String execString = getARDUINOExec();

    if (args.size() > 0) {
      setDefaultBoard((String) args.get(0));
    }
    if (getDefaultBoard() != null) {
      execString += " --board " + getDefaultBoard();
      if (args.size() > 1) {
        setDefaultPort((String) args.get(1));
      }
    }
    if (getDefaultPort() != null) {
      execString += " --port " + getDefaultPort();
    }

    final File ino = findIno(code);
    if (ino == null) {
      return null;
    }

    execString += " --upload " + ino.getAbsolutePath().replaceAll(" ", "\\ ");

    if (!loadedBowler) {
      loadedBowler = true;
      run(getARDUINOExec() + " --install-library BowlerCom");
    }
    run(execString);

    return null;
  }

  public static void installBoard(final String product, final String arch) throws Exception {
    run(getARDUINOExec() + " --install-boards " + product + ":" + arch);
  }

  public static void installLibrary(final String lib) throws Exception {
    run(getARDUINOExec() + " --install-library " + lib);
  }

  private static void run(final String execString) throws Exception {
    System.out.println("Running:\n" + execString);
    // Get runtime
    final Runtime rt = Runtime.getRuntime();
    // Start a new process
    final Process p = rt.exec(execString);
    // You can or maybe should wait for the process to complete
    p.waitFor();
    // Get process' output: its InputStream
    final InputStream is = p.getInputStream();
    final InputStream err = p.getInputStream();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    final BufferedReader readerErr = new BufferedReader(new InputStreamReader(err));

    // And print each line
    String s = null;
    while ((s = reader.readLine()) != null) {
      System.out.println(s); // This is how the scripts output to the print stream
    }

    s = null;
    while ((s = readerErr.readLine()) != null) {
      System.out.println(s); // This is how the scripts output to the print stream
    }
    is.close();
    err.close();
  }

  private File findIno(final File start) {
    if (start == null) {
      return null;
    }
    if (start.getName().endsWith(".ino")) {
      return start;
    } else {
      final File dir = start.getParentFile();
      if (dir != null) {
        for (final File f : dir.listFiles()) {
          if (findIno(f) != null) {
            return f;
          }
        }
      }
    }
    return null;
  }

  @Override
  public Object inlineScriptRun(final String code, final ArrayList<Object> args) {
    return null;
  }

  @Override
  public String getShellType() {
    return "Arduino";
  }

  @Override
  public boolean getIsTextFile() {
    return true;
  }

  private static String getDefaultPort() {
    return defaultPort;
  }

  private static void setDefaultPort(final String defaultPort) {
    ArduinoLoader.defaultPort = defaultPort;
  }

  private static String getDefaultBoard() {
    return defaultBoard;
  }

  private static void setDefaultBoard(final String defaultBoard) {
    ArduinoLoader.defaultBoard = defaultBoard;
  }

  private static String getARDUINOExec() {
    return ARDUINO;
  }

  public static void setARDUINOExec(final String aRDUINO) {
    ARDUINO = aRDUINO;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    return new ArrayList<>(Arrays.asList("c", "ino", "h", "cpp", "hpp"));
  }
}
