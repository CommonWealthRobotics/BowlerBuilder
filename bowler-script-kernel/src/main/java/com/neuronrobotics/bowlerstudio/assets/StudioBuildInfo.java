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
package com.neuronrobotics.bowlerstudio.assets;

import com.neuronrobotics.bowlerstudio.BowlerKernel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StudioBuildInfo {

  private static Class baseBuildInfoClass = BowlerKernel.class;

  public static String getVersion() {
    final String s = getTag("app.version");

    if (s == null) {
      throw new RuntimeException("Failed to load version number");
    }
    return s;
  }

  private static int getProtocolVersion() {
    return getBuildInfo()[0];
  }

  private static int getSDKVersion() {
    return getBuildInfo()[1];
  }

  private static int getBuildVersion() {
    return getBuildInfo()[2];
  }

  private static int[] getBuildInfo() {
    try {
      final String s = getVersion();
      final String[] splits = s.split("[.]+");
      final int[] rev = new int[3];
      for (int i = 0; i < 3; i++) {
        rev[i] = new Integer(splits[i]);
      }
      return rev;
    } catch (final NumberFormatException e) {
      return new int[] {0, 0, 0};
    }
  }

  private static String getTag(final String target) {
    try {
      final StringBuilder s = new StringBuilder();
      final InputStream is = getBuildPropertiesStream();
      final BufferedReader br = new BufferedReader(new InputStreamReader(is));

      String line;
      try {
        while (null != (line = br.readLine())) {
          s.append(line).append("\n");
        }
      } catch (final IOException ignored) {
      }

      final String[] splitAll = s.toString().split("[\n]+");
      for (final String aSplitAll : splitAll) {
        if (aSplitAll.contains(target)) {
          final String[] split = aSplitAll.split("[=]+");
          return split[1];
        }
      }
    } catch (final NullPointerException e) {
      return null;
    }
    return null;
  }

  public static String getBuildDate() {
    String s = "";
    final InputStream is = StudioBuildInfo.class.getResourceAsStream("/META-INF/MANIFEST.MF");
    final BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    try {
      while (null != (line = br.readLine())) {
        s += line + "\n";
      }
    } catch (final IOException ignored) {
    }
    // System.out.println("Manifest:\n"+s);
    return "";
  }

  private static InputStream getBuildPropertiesStream() {
    return baseBuildInfoClass.getResourceAsStream("build.properties");
  }

  public static String getSDKVersionString() {
    return getName();
  }

  public static boolean isOS64bit() {
    return (System.getProperty("os.arch").contains("x86_64"));
  }

  public static boolean isARM() {
    return (System.getProperty("os.arch").toLowerCase().contains("arm"));
  }

  private static boolean isLinux() {
    return (System.getProperty("os.name").toLowerCase().contains("linux"));
  }

  public static boolean isWindows() {
    return (System.getProperty("os.name").toLowerCase().contains("win"));
  }

  private static boolean isMac() {
    return (System.getProperty("os.name").toLowerCase().contains("mac"));
  }

  public static boolean isUnix() {
    return (isLinux() || isMac());
  }

  public static Class getBaseBuildInfoClass() {
    return baseBuildInfoClass;
  }

  public static void setBaseBuildInfoClass(final Class c) {
    baseBuildInfoClass = c;
  }

  private static String getName() {
    return "Bowler Studio "
        + getProtocolVersion()
        + "."
        + getSDKVersion()
        + "("
        + getBuildVersion()
        + ")";
  }
}
