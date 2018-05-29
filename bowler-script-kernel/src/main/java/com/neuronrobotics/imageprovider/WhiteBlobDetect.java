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
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

public class WhiteBlobDetect implements IObjectDetector {

  MatOfKeyPoint matOfKeyPoints = new MatOfKeyPoint();
  Mat prethresh = new Mat();
  Mat postthresh = new Mat();
  private FeatureDetector rgbBlobDetector =
      FeatureDetector.create(FeatureDetector.PYRAMID_SIMPLEBLOB);
  private int minSize;
  private int maxSize;
  Scalar colorKey;

  public WhiteBlobDetect(int minSize, int maxSize, Scalar lower) {
    this.minSize = minSize;
    this.maxSize = maxSize;
    colorKey = lower.clone();
  }

  public List<Detection> getObjects(BufferedImage in, BufferedImage disp) {
    Mat inputImage = new Mat();
    OpenCVImageConversionFactory.bufferedImageToMat(in, inputImage);
    Mat displayImage = new Mat();
    ArrayList<Detection> ret = new ArrayList<>();
    KeyPoint[] detects = getObjects(inputImage, displayImage);
    for (int i = 0; i < detects.length; i++) {
      ret.add(new Detection(detects[i].pt.x, detects[i].pt.y, detects[i].size));
    }
    return ret;
  }

  private KeyPoint[] getObjects(Mat inputImage, Mat displayImage) {

    Imgproc.cvtColor(inputImage, prethresh, Imgproc.COLOR_RGB2GRAY);
    Imgproc.threshold(
        prethresh, postthresh, colorKey.val[1], colorKey.val[0], Imgproc.THRESH_BINARY);

    Mat invertcolormatrix =
        new Mat(postthresh.rows(), postthresh.cols(), postthresh.type(), new Scalar(255, 255, 255));
    Core.subtract(invertcolormatrix, postthresh, postthresh);

    rgbBlobDetector.detect(postthresh, matOfKeyPoints);

    postthresh.copyTo(displayImage);
    Features2d.drawKeypoints(postthresh, matOfKeyPoints, displayImage, new Scalar(0, 0, 255, 0), 0);

    // Prepare to display data
    int useful = 0;
    KeyPoint[] keyPoints = matOfKeyPoints.toArray();

    if (keyPoints.length > 0) {

      for (int i = 0; i < keyPoints.length; i++) {
        if (keyPoints[i].size > minSize && keyPoints[i].size < maxSize) {
          useful++;
        }
      }

      // System.out.println("Found "+useful);
      KeyPoint[] myArray = new KeyPoint[useful];
      Point center = null; //

      useful = 0;
      for (int i = 0; i < keyPoints.length; i++) {
        if (keyPoints[i].size > minSize && keyPoints[i].size < maxSize) {
          // System.out.println("Data from blob detect "+keyPoints[i]);

          center = keyPoints[i].pt;

          Size objectSize = new Size(keyPoints[i].size, keyPoints[i].size);

          Core.ellipse(
              displayImage, center, objectSize, 0, 0, 360, new Scalar(255, 0, 255), 4, 8, 0);

          myArray[useful++] = keyPoints[i];
        }
      }
      if (center != null) {
        for (int i = 0; i < myArray.length; i++) {
          if (myArray[i] != null) {

            Core.line(
                displayImage,
                new Point(150, 50),
                myArray[i].pt,
                new Scalar(100, 10, 10) /* CV_BGR(100,10,10) */,
                3);
            Core.circle(displayImage, myArray[i].pt, 10, new Scalar(100, 10, 10), 3);
          }
        }
      }
      return myArray;
    }

    System.out.println("Got: " + matOfKeyPoints.size());

    return new KeyPoint[0];
  }
}
