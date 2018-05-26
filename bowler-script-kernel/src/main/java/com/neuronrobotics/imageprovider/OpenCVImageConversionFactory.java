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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javafx.scene.image.Image;

public class OpenCVImageConversionFactory {

  /**
   * Converts/writes a Mat into a BufferedImage.
   *
   * @param matrix Mat of type CV_8UC3 or CV_8UC1
   * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
   */
  public static BufferedImage matToBufferedImage(org.opencv.core.Mat matrix) {
    int cols = matrix.cols();
    int rows = matrix.rows();
    int elemSize = (int) matrix.elemSize();
    byte[] data = new byte[cols * rows * elemSize];
    int type;
    matrix.get(0, 0, data);
    switch (matrix.channels()) {
      case 1:
        type = BufferedImage.TYPE_BYTE_GRAY;
        break;
      case 3:
        type = BufferedImage.TYPE_3BYTE_BGR;
        // bgr to rgb
        byte b;
        for (int i = 0; i < data.length; i = i + 3) {
          b = data[i];
          data[i] = data[i + 2];
          data[i + 2] = b;
        }
        break;
      default:
        return null;
    }
    BufferedImage image2 = new BufferedImage(cols, rows, type);
    image2.getRaster().setDataElements(0, 0, cols, rows, data);
    return image2;
  }

  public static void deepCopy(org.opencv.core.Mat src, BufferedImage dest) {
    Graphics g = dest.createGraphics();
    g.drawImage(matToBufferedImage(src), 0, 0, null);
  }

  /**
   * Converts/writes a Mat into a BufferedImage.
   *
   * @param matrix Mat of type CV_8UC3 or CV_8UC1
   * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
   */
  public static Image matToJfxImage(org.opencv.core.Mat matrix) {

    return AbstractImageProvider.getJfxImage(matToBufferedImage(matrix));
  }

  public static void bufferedImageToMat(BufferedImage input, org.opencv.core.Mat output) {
    org.opencv.core.Mat mb;

    byte[] tmpByteArray = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
    mb = new org.opencv.core.Mat(input.getHeight(), input.getWidth(), 16); // 8uc3
    mb.put(0, 0, tmpByteArray);
    mb.copyTo(output);
  }
}
