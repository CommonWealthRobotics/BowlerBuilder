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
package com.neuronrobotics.imageprovider;

import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCVJNILoader {

  static NativeResource resource = null;

  /** Loads the OpenCV JNI. */
  public static void load() {
    if (resource != null) {
      return;
    }
    resource = new NativeResource();
    // +Core.NATIVE_LIBRARY_NAME+".so"
    // +Core.NATIVE_LIBRARY_NAME+".so"
    if (NativeResource.isLinux()) {
      String[] possibleLocals =
          new String[] {"/usr/local/share/OpenCV/java/lib/", "/usr/lib/jni/lib/", "/usr/lib/jni/"};
      String erBack = "";
      for (String local : possibleLocals) {
        File libDirectory = new File(local);
        if (libDirectory.isDirectory()) {
          File[] possibleLibs = libDirectory.listFiles();
          for (File f : possibleLibs) {
            // System.out.println("Checking file: "+f);
            if (!f.isDirectory()
                && f.getName().contains("opencv_java24")
                && f.getName().endsWith(".so")) {
              try {
                System.load(f.getAbsolutePath());
                Mat m = Mat.eye(3, 3, CvType.CV_8UC1);
                System.out.println("Loading opencv lib " + f.getAbsolutePath());
                return;
              } catch (Error e) {
                // try the next one
                erBack += " " + e.getMessage();
                e.printStackTrace();
              }
            }
          }

        } else {
          erBack += "No file " + local;
        }
      }

      throw new RuntimeException(erBack);
    } else if (NativeResource.isWindows()) {
      String basedir = System.getenv("OPENCV_DIR");
      if (basedir == null) {
        throw new RuntimeException(
            "OPENCV_DIR was not found, environment variable OPENCV_DIR needs to be set");
      }
      System.err.println("OPENCV_DIR found at " + basedir);
      if ((!System.getProperty("sun.arch.data.model").contains("32") && basedir.contains("x64"))) {

        basedir.replace("x64", "x86");
        System.err.println("OPENCV_DIR environment variable is not set correctly");
      }
      basedir += "\\..\\..\\java\\";
      // if(basedir.contains("x64")){
      System.load(basedir + "x64\\" + Core.NATIVE_LIBRARY_NAME + ".dll");
      // }else{
      //    System.load(basedir+"x86\\"+Core.NATIVE_LIBRARY_NAME+".dll");
      // }
    } else if (NativeResource.isOSX()) {
      String basedir = System.getenv("OPENCV_DIR");
      if (basedir == null) {
        throw new RuntimeException(
            "OPENCV_DIR was not found, environment variable OPENCV_DIR needs to be set");
      }
      // basedir="/Users/hephaestus/Desktop/opencv249build/";
      String lib = basedir.trim() + "/lib/lib" + Core.NATIVE_LIBRARY_NAME + ".dylib";
      System.err.println("OPENCV_DIR found at " + lib);
      System.load(lib);
    }
  }
}
