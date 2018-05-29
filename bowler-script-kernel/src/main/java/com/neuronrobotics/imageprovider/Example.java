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
import java.util.List;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Example {

  public static void main(String arg[]) {
    // Load the native library.
    System.loadLibrary("opencv_java245");

    // It is better to group all frames together so cut and paste to
    // create more frames is easier
    JFrame frame1 = new JFrame("Camera");
    frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame1.setSize(640, 480);
    frame1.setBounds(0, 0, frame1.getWidth(), frame1.getHeight());
    Panel panel1 = new Panel();
    frame1.setContentPane(panel1);
    frame1.setVisible(true);
    JFrame frame2 = new JFrame("HSV");
    frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame2.setSize(640, 480);
    frame2.setBounds(300, 100, frame2.getWidth() + 300, 100 + frame2.getHeight());
    Panel panel2 = new Panel();
    frame2.setContentPane(panel2);
    frame2.setVisible(true);
    JFrame frame3 = new JFrame("S,V Distance");
    frame3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame3.setSize(640, 480);
    frame3.setBounds(600, 200, frame3.getWidth() + 600, 200 + frame3.getHeight());
    Panel panel3 = new Panel();
    frame3.setContentPane(panel3);
    frame3.setVisible(true);
    JFrame frame4 = new JFrame("Threshold");
    frame4.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame4.setSize(640, 480);
    frame4.setBounds(900, 300, frame3.getWidth() + 900, 300 + frame3.getHeight());
    Panel panel4 = new Panel();
    frame4.setContentPane(panel4);
    frame4.setVisible(true);

    // -- 2. Read the video stream
    VideoCapture capture = new VideoCapture(0);
    Mat webcamImage = new Mat();
    Mat hsvImage = new Mat();
    Mat thresholded = new Mat();
    Mat thresholded2 = new Mat();
    capture.read(webcamImage);
    frame1.setSize(webcamImage.width() + 40, webcamImage.height() + 60);
    frame2.setSize(webcamImage.width() + 40, webcamImage.height() + 60);
    frame3.setSize(webcamImage.width() + 40, webcamImage.height() + 60);
    frame4.setSize(webcamImage.width() + 40, webcamImage.height() + 60);
    Mat array255 = new Mat(webcamImage.height(), webcamImage.width(), CvType.CV_8UC1);
    array255.setTo(new Scalar(255));
    /*Mat S=new Mat();
    S.ones(new Size(hsv_image.width(),hsv_image.height()),CvType.CV_8UC1);
    Mat V=new Mat();
    V.ones(new Size(hsv_image.width(),hsv_image.height()),CvType.CV_8UC1);
        Mat H=new Mat();
    H.ones(new Size(hsv_image.width(),hsv_image.height()),CvType.CV_8UC1);*/
    Mat distance = new Mat(webcamImage.height(), webcamImage.width(), CvType.CV_8UC1);
    // new Mat();//new Size(webcam_image.width(),webcam_image.height()),CvType.CV_8UC1);
    List<Mat> lhsv = new ArrayList<>(3);
    Mat circles = new Mat(); // No need (and don't know how) to initialize it.
    // The function later will do it... (to a 1*N*CV_32FC3)
    Scalar hsvMin = new Scalar(0, 50, 50, 0);
    Scalar hsvMax = new Scalar(6, 255, 255, 0);
    Scalar hsvMin2 = new Scalar(175, 50, 50, 0);
    Scalar hsvMax2 = new Scalar(179, 255, 255, 0);

    double[] data;
    if (capture.isOpened()) {
      while (true) {
        capture.read(webcamImage);
        if (!webcamImage.empty()) {
          // One way to select a range of colors by Hue
          Imgproc.cvtColor(webcamImage, hsvImage, Imgproc.COLOR_BGR2HSV);
          Core.inRange(hsvImage, hsvMin, hsvMax, thresholded);
          Core.inRange(hsvImage, hsvMin2, hsvMax2, thresholded2);
          Core.bitwise_or(thresholded, thresholded2, thresholded);
          // Notice that the thresholds don't really work as a "distance"
          // Ideally we would like to cut the image by hue and then pick just
          // the area where S combined V are largest.
          // Strictly speaking, this would be something like sqrt((255-S)^2+(255-V)^2)>Range
          // But if we want to be "faster" we can do just (255-S)+(255-V)>Range
          // Or otherwise 510-S-V>Range
          // Anyhow, we do the following... Will see how fast it goes...
          Core.split(hsvImage, lhsv); // We get 3 2D one channel Mats
          Mat sMat = lhsv.get(1);
          Mat vMat = lhsv.get(2);
          Core.subtract(array255, sMat, sMat);
          Core.subtract(array255, vMat, vMat);
          sMat.convertTo(sMat, CvType.CV_32F);
          vMat.convertTo(vMat, CvType.CV_32F);
          Core.magnitude(sMat, vMat, distance);
          Core.inRange(distance, new Scalar(0.0), new Scalar(200.0), thresholded2);
          Core.bitwise_and(thresholded, thresholded2, thresholded);
          // Apply the Hough Transform to find the circles
          Imgproc.GaussianBlur(thresholded, thresholded, new Size(9, 9), 0, 0);
          Imgproc.HoughCircles(
              thresholded,
              circles,
              Imgproc.CV_HOUGH_GRADIENT,
              2,
              thresholded.height() / 4,
              500,
              50,
              0,
              0);
          // Imgproc.Canny(thresholded, thresholded, 500, 250);
          // -- 4. Add some info to the image
          Core.line(
              webcamImage,
              new Point(150, 50),
              new Point(202, 200),
              new Scalar(100, 10, 10) /*CV_BGR(100,10,10)*/,
              3);
          Core.circle(webcamImage, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
          data = webcamImage.get(210, 210);
          Core.putText(
              webcamImage,
              String.format(
                  "("
                      + String.valueOf(data[0])
                      + ","
                      + String.valueOf(data[1])
                      + ","
                      + String.valueOf(data[2])
                      + ")"),
              new Point(30, 30),
              3, // FONT_HERSHEY_SCRIPT_SIMPLEX
              1.0,
              new Scalar(100, 10, 10, 255),
              3);
          // int cols = circles.cols();
          int rows = circles.rows();
          int elemSize = (int) circles.elemSize(); // Returns 12 (3 * 4bytes in a float)
          float[] data2 = new float[rows * elemSize / 4];
          if (data2.length > 0) {
            circles.get(0, 0, data2); // Points to the first element and reads the whole thing
            // into data2
            for (int i = 0; i < data2.length; i = i + 3) {
              Point center = new Point(data2[i], data2[i + 1]);
              // Core.ellipse( this, center, new Size( rect.width*0.5, rect.height*0.5), 0, 0, 360,
              // new Scalar( 255, 0, 255 ), 4, 8, 0 );
              Core.ellipse(
                  webcamImage,
                  center,
                  new Size((double) data2[i + 2], (double) data2[i + 2]),
                  0,
                  0,
                  360,
                  new Scalar(255, 0, 255),
                  4,
                  8,
                  0);
            }
          }
          Core.line(
              hsvImage,
              new Point(150, 50),
              new Point(202, 200),
              new Scalar(100, 10, 10), /*CV_BGR(100,10,10)*/
              3);
          Core.circle(hsvImage, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
          data = hsvImage.get(210, 210);
          Core.putText(
              hsvImage,
              String.format(
                  "("
                      + String.valueOf(data[0])
                      + ","
                      + String.valueOf(data[1])
                      + ","
                      + String.valueOf(data[2])
                      + ")"),
              new Point(30, 30),
              3, // FONT_HERSHEY_SCRIPT_SIMPLEX
              1.0,
              new Scalar(100, 10, 10, 255),
              3);
          distance.convertTo(distance, CvType.CV_8UC1);
          Core.line(
              distance,
              new Point(150, 50),
              new Point(202, 200),
              new Scalar(100) /*CV_BGR(100,10,10)*/,
              3);
          Core.circle(distance, new Point(210, 210), 10, new Scalar(100), 3);
          data = (double[]) distance.get(210, 210);
          Core.putText(
              distance,
              String.format("(" + String.valueOf(data[0]) + ")"),
              new Point(30, 30),
              3, // FONT_HERSHEY_SCRIPT_SIMPLEX
              1.0,
              new Scalar(100),
              3);
          // -- 5. Display the image
          panel1.setimagewithMat(webcamImage);
          panel2.setimagewithMat(hsvImage);
          // panel2.setimagewithMat(S);
          // distance.convertTo(distance, CvType.CV_8UC1);
          panel3.setimagewithMat(distance);
          panel4.setimagewithMat(thresholded);
          frame1.repaint();
          frame2.repaint();
          frame3.repaint();
          frame4.repaint();
        } else {
          System.out.println(" --(!) No captured frame -- Break!");
          break;
        }
      }
    }
    return;
  }
}
