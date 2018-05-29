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

import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

class DetectionUtil {

  static void labelDetectedObjects(
      Mat displayImage,
      float[] data2,
      ArrayList<Detection> myArray,
      Point center,
      Mat circles,
      double[] data) {
    if (data2.length > 0) {
      // Points to the first element and reads the whole thing into data2
      circles.get(0, 0, data2);
      for (int i = 0; i < data2.length; i = i + 3) {
        center = new Point(data2[i], data2[i + 1]);
        Size objectSize = new Size((double) data2[i + 2], (double) data2[i + 2]);
        // Core.ellipse( this, center, new Size( rect.width*0.5,
        // rect.height*0.5), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8,
        // 0 );
        Core.ellipse(displayImage, center, objectSize, 0, 0, 360, new Scalar(255, 0, 255), 4, 8, 0);
        myArray.add(new Detection(center.x, center.y, objectSize.height));
      }

      if (center != null) {
        Core.putText(
            displayImage,
            "Circles ("
                + String.valueOf(data[0])
                + ","
                + String.valueOf(data[1])
                + ","
                + String.valueOf(data[2])
                + ")",
            new Point(30, 30),
            2, // FONT_HERSHEY_SCRIPT_SIMPLEX
            .5,
            new Scalar(100, 10, 10, 255),
            3);

        for (Detection object : myArray) {
          Point centerTmp = new Point(object.getX(), object.getY());
          Core.line(
              displayImage,
              new Point(150, 50),
              centerTmp,
              new Scalar(100, 10, 10), /* CV_BGR(100,10,10) */
              3);
          Core.circle(displayImage, centerTmp, 10, new Scalar(100, 10, 10), 3);
        }
      }
    }
  }
}
