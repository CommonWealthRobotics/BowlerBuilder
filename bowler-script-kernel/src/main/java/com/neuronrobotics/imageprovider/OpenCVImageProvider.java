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
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class OpenCVImageProvider extends AbstractImageProvider {

  private VideoCapture vc;
  private int camerIndex;
  Mat m = new Mat();

  public OpenCVImageProvider(int camerIndex) {
    this.camerIndex = camerIndex;
    setVc(new VideoCapture(camerIndex));

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (!getVc().isOpened()) {
      System.out.println("Camera Error");
    } else {
      //			boolean wset = getVc().set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
      //			boolean hset = getVc().set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);
      System.out.println(
          "Camera OK at "
              + " width: "
              + getVc().get(Highgui.CV_CAP_PROP_FRAME_WIDTH)
              + " height: "
              + getVc().get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
    }
  }

  @Override
  public String toString() {
    String s = "OpenCVImageProvider on camera " + camerIndex + " " + getVc().toString();
    return s;
  }

  @Override
  public boolean captureNewImage(BufferedImage imageData) {
    if (!getVc().isOpened()) {
      return false;
    }

    try {
      AbstractImageProvider.deepCopy(captureNewImage(), imageData);
    } catch (Exception ex) {
      if (InterruptedException.class.isInstance(ex)) {
        throw new RuntimeException(ex);
      }
    }
    return true;
  }

  private VideoCapture getVc() {
    return vc;
  }

  private void setVc(VideoCapture vc) {
    this.vc = vc;
  }

  @Override
  public void disconnectDeviceImp() {
    if (vc != null) {
      vc.release();
    }
    setVc(null);
  }

  @Override
  public boolean connectDeviceImp() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ArrayList<String> getNamespacesImp() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BufferedImage captureNewImage() {
    getVc().read(m);
    return OpenCVImageConversionFactory.matToBufferedImage(m);
  }
}
