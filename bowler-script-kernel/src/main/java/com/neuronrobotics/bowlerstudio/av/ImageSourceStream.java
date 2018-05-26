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
package com.neuronrobotics.bowlerstudio.av;

import java.awt.Dimension;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

/** The source stream to go along with ImageDataSource. */
class ImageSourceStream implements PullBufferStream {

  private Vector images;
  private int width;
  private int height;
  private VideoFormat format;

  private int nextImage = 0; // index of the next image to be read.
  private boolean ended = false;

  public ImageSourceStream(final int width, final int height, final int frameRate, final Vector images) {
    this.width = width;
    this.height = height;
    this.images = images;

    format =
        new VideoFormat(
            VideoFormat.JPEG,
            new Dimension(width, height),
            Format.NOT_SPECIFIED,
            Format.byteArray,
            (float) frameRate);
  }

  /** We should never need to block assuming data are read from files. */
  public boolean willReadBlock() {
    return false;
  }

  /** This is called from the Processor to read a frame worth of video data. */
  public void read(final Buffer buf) throws IOException {

    // Check if we've finished all the frames.
    if (nextImage >= images.size()) {
      // We are done. Set EndOfMedia.
      System.err.println("Done reading all images.");
      buf.setEOM(true);
      buf.setOffset(0);
      buf.setLength(0);
      ended = true;
      return;
    }

    final String imageFile = (String) images.elementAt(nextImage);
    nextImage++;

    System.err.println("  - reading image file: " + imageFile);

    // Open a random access file for the next image.
    final RandomAccessFile raFile;
    raFile = new RandomAccessFile(imageFile, "r");

    byte data[] = null;

    // Check the input buffer type & size.

    if (buf.getData() instanceof byte[]) {
      data = (byte[]) buf.getData();
    }

    // Check to see the given buffer is big enough for the frame.
    if (data == null || data.length < raFile.length()) {
      data = new byte[(int) raFile.length()];
      buf.setData(data);
    }

    // Read the entire JPEG image from the file.
    raFile.readFully(data, 0, (int) raFile.length());

    System.err.println("    read " + raFile.length() + " bytes.");

    buf.setOffset(0);
    buf.setLength((int) raFile.length());
    buf.setFormat(format);
    buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);

    // Close the random access file.
    raFile.close();
  }

  /** Return the format of each video frame. That will be JPEG. */
  public Format getFormat() {
    return format;
  }

  public ContentDescriptor getContentDescriptor() {
    return new ContentDescriptor(ContentDescriptor.RAW);
  }

  public long getContentLength() {
    return 0;
  }

  public boolean endOfStream() {
    return ended;
  }

  public Object[] getControls() {
    return new Object[0];
  }

  public Object getControl(final String type) {
    return null;
  }
}
