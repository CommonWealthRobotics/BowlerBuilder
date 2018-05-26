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
package com.neuronrobotics.bowlerkernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/** The Class SDKBuildInfo. */
public class BowlerKernelBuildInfo {

  /** The Constant NAME. */
  private static final String NAME =
      "CommonWealthRobotics SDK "
          + getProtocolVersion()
          + "."
          + getSDKVersion()
          + "("
          + getBuildVersion()
          + ")";

  /**
   * Gets the version.
   *
   * @return the version
   */
  public static String getVersion() {
    String s = getTag("app.version");
    if (s == null) {
      s = "0.0.0";
    }
    return s;
  }

  /**
   * Gets the protocol version.
   *
   * @return the protocol version
   */
  public static int getProtocolVersion() {
    return getBuildInfo()[0];
  }

  /**
   * Gets the SDK version.
   *
   * @return the SDK version
   */
  public static int getSDKVersion() {
    return getBuildInfo()[1];
  }

  /**
   * Gets the builds the version.
   *
   * @return the builds the version
   */
  public static int getBuildVersion() {
    return getBuildInfo()[2];
  }

  /**
   * Gets the builds the info.
   *
   * @return the builds the info
   */
  public static int[] getBuildInfo() {
    String s = getVersion();
    String[] splits = s.split("[.]+");
    int[] rev = new int[3];
    for (int i = 0; i < 3; i++) {
      rev[i] = new Integer(splits[i]);
    }
    return rev;
  }

  /**
   * Gets the tag.
   *
   * @param target the target
   * @return the tag
   */
  private static String getTag(String target) {
    try {
      String s = "";
      InputStream is = getBuildPropertiesStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line;
      try {
        while (null != (line = br.readLine())) {
          s += line + "\n";
        }
      } catch (IOException e) {
      }
      String[] splitAll = s.split("[\n]+");
      for (int i = 0; i < splitAll.length; i++) {
        if (splitAll[i].contains(target)) {
          String[] split = splitAll[i].split("[=]+");
          return split[1];
        }
      }
    } catch (NullPointerException e) {
      return null;
    }
    return null;
  }

  /**
   * Gets the builds the date.
   *
   * @return the builds the date
   */
  public static String getBuildDate() {
    String s = "";
    InputStream is = BowlerKernelBuildInfo.class.getResourceAsStream("/META-INF/MANIFEST.MF");
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    try {
      while (null != (line = br.readLine())) {
        s += line + "\n";
      }
    } catch (IOException e) {
    }
    // System.out.println("Manifest:\n"+s);
    return "";
  }

  /**
   * Gets the builds the properties stream.
   *
   * @return the builds the properties stream
   */
  private static InputStream getBuildPropertiesStream() {
    return BowlerKernelBuildInfo.class.getResourceAsStream("build.properties");
  }

  /**
   * Gets the SDK version string.
   *
   * @return the SDK version string
   */
  public static String getSDKVersionString() {
    return NAME;
  }

  /**
   * Checks if is o s64bit.
   *
   * @return true, if is o s64bit
   */
  public static boolean isOS64bit() {
    return (System.getProperty("os.arch").indexOf("x86_64") != -1);
  }

  /**
   * Checks if is arm.
   *
   * @return true, if is arm
   */
  public static boolean isARM() {
    return (System.getProperty("os.arch").toLowerCase().indexOf("arm") != -1);
  }

  /**
   * Checks if is linux.
   *
   * @return true, if is linux
   */
  public static boolean isLinux() {
    return (System.getProperty("os.name").toLowerCase().indexOf("linux") != -1);
  }

  /**
   * Checks if is windows.
   *
   * @return true, if is windows
   */
  public static boolean isWindows() {
    return (System.getProperty("os.name").toLowerCase().indexOf("win") != -1);
  }

  /**
   * Checks if is mac.
   *
   * @return true, if is mac
   */
  public static boolean isMac() {
    return (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1);
  }

  /**
   * Checks if is unix.
   *
   * @return true, if is unix
   */
  public static boolean isUnix() {
    return (isLinux() || isMac());
  }
}
