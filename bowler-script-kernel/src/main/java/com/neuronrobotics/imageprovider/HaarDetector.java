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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class HaarDetector implements IObjectDetector {

  private MatOfRect faceDetections = new MatOfRect();
  private CascadeClassifier faceDetector;
  private double scale = .3;

  public HaarDetector(File cascade) {
    // Create a face detector from the cascade file in the resources
    // directory.
    faceDetector = new CascadeClassifier(cascade.getAbsolutePath());
  }

  /**
   * Run object detection.
   *
   * @param in image to read from
   * @param disp image to label detected objects on
   * @return a list of detected objects
   */
  public List<Detection> getObjects(BufferedImage in, BufferedImage disp) {
    Mat inputImage = new Mat();
    OpenCVImageConversionFactory.bufferedImageToMat(in, inputImage);
    try {
      Mat localImage = new Mat();
      Size s = new Size(in.getWidth(), in.getHeight());
      Imgproc.resize(inputImage, localImage, new Size(s.width * scale, s.height * scale));
      Imgproc.cvtColor(localImage, localImage, Imgproc.COLOR_BGR2GRAY);

      faceDetector.detectMultiScale(localImage, faceDetections);
      Rect[] smallArray = faceDetections.toArray();
      ArrayList<Detection> myArray = new ArrayList<>();

      for (int i = 0; i < smallArray.length; i++) {
        Rect r = smallArray[i];
        myArray.add(new Detection((r.x / scale), (r.y / scale), (r.width / scale)));
      }
      Mat displayImage = new Mat();
      OpenCVImageConversionFactory.bufferedImageToMat(disp, displayImage);
      Point center = null; //
      // System.out.println(String.format("Detected %s faces", myArray.length));
      // Draw a bounding box around each face.
      for (Detection rect : myArray) {
        // Core.rectangle(displayImage, rect.pt, new Point(rect.x + rect.width, rect.y +
        // rect.height), new Scalar(0, 255, 0));
        center = new Point(rect.getX() + (rect.getSize() / 2), rect.getY() + (rect.getSize() / 2));

        Size objectSize = new Size((rect.getSize() / 2), (rect.getSize() / 2));

        Core.ellipse(displayImage, center, objectSize, 0, 0, 360, new Scalar(255, 0, 255), 4, 8, 0);
      }
      OpenCVImageConversionFactory.matToBufferedImage(displayImage).copyData(disp.getRaster());

      return myArray;
    } catch (CvException | NullPointerException | IllegalArgumentException e2) {
      // startup noise
      // e.printStackTrace();
      return new ArrayList<>();
    }
  }
}
