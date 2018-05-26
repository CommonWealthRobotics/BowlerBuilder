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

import java.util.Vector;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

/**
 * A DataSource to read from a list of JPEG image files and turn that into a stream of JMF buffers.
 * The DataSource is not seekable or positionable.
 */
class ImageDataSource extends PullBufferDataSource {

  private ImageSourceStream[] streams;

  ImageDataSource(final int width, final int height, final int frameRate, final Vector images) {
    streams = new ImageSourceStream[1];
    streams[0] = new ImageSourceStream(width, height, frameRate, images);
  }

  public void setLocator(final MediaLocator source) {}

  public MediaLocator getLocator() {
    return null;
  }

  /**
   * Content type is of RAW since we are sending buffers of video frames without a container format.
   */
  public String getContentType() {
    return ContentDescriptor.RAW;
  }

  public void connect() {}

  public void disconnect() {}

  public void start() {}

  public void stop() {}

  /** Return the ImageSourceStreams. */
  public PullBufferStream[] getStreams() {
    return streams;
  }

  /**
   * We could have derived the duration from the number of frames and frame rate. But for the
   * purpose of this program, it's not necessary.
   */
  public Time getDuration() {
    return DURATION_UNKNOWN;
  }

  public Object[] getControls() {
    return new Object[0];
  }

  public Object getControl(final String type) {
    return null;
  }
}
