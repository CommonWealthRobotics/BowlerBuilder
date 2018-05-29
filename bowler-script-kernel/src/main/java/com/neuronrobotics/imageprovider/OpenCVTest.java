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

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Tab;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.opencv.core.Scalar;

// any java file starting with i is the interface

public class OpenCVTest {

  private ArrayList<IObjectDetector> detectors;
  private RGBColorDetector mainFilter;

  public void run() {

    File harr = null;
    try {
      harr =
          ScriptingEngine.fileFromGit(
              "https://github.com/madhephaestus/DefaultHaarCascade.git",
              "haarcascade_frontalface_default.xml");
    } catch (InvalidRemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TransportException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (GitAPIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    HaarDetector faceDetectorObject = new HaarDetector(harr);

    BufferedImage inputImage = AbstractImageProvider.newBufferImage(640, 480);
    BufferedImage displayImage = AbstractImageProvider.newBufferImage(640, 480);

    JFrame frame = new JFrame();

    JTabbedPane tabs = new JTabbedPane();
    frame.setContentPane(tabs);
    frame.setSize(640, 580);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    detectors = new ArrayList<>();
    ArrayList<AbstractImageProvider> imageProviders = new ArrayList<>();
    ArrayList<ImageIcon> iconsCaptured = new ArrayList<>();
    ArrayList<ImageIcon> iconsProcessed = new ArrayList<>();

    Scalar upper = new Scalar(30, 150, 0, 0);
    Scalar lower = new Scalar(240, 166, 0, 0);

    Scalar upper1 = new Scalar(360, 255, 255, 0);
    Scalar lower1 = new Scalar(240, 0, 0, 0);

    imageProviders.add(new OpenCVImageProvider(0)); // Image provider
    imageProviders.get(0).getLatestImage(inputImage, displayImage);
    // Provides the static file to the processors
    imageProviders.add(new StaticFileProvider(new File("image.png")));

    //        mainFilter = new RGBColorDetector(inputImage,
    //                lower,
    //                upper,
    //                lower1,
    //                upper1);

    // add human detector later
    // detectors.add(faceDetectorObject);
    detectors.add(new SalientDetector());
    // detectors.add(new WhiteBlobDetect((int) upper.val[0],(int) upper.val[1], lower));
    Tab t = new Tab();

    int x = 0;
    for (AbstractImageProvider img : imageProviders) {
      img.getLatestImage(inputImage, displayImage);

      ImageIcon tmp = new ImageIcon(img.getLatestImage());
      iconsCaptured.add(tmp);

      tabs.addTab("Camera " + x, new JLabel(tmp));

      for (int i = 0; i < detectors.size(); i++) {
        detectors.get(i).getObjects(inputImage, displayImage);
        ImageIcon ptmp = new ImageIcon(img.getLatestImage());
        iconsProcessed.add(ptmp);
        tabs.addTab("Processed " + x + "." + i, new JLabel(ptmp));
      }
      x++;
    }

    while (true) {
      try {
        for (int i = 0; i < imageProviders.size(); i++) { // list of image provid
          imageProviders.get(i).getLatestImage(inputImage, displayImage); // capture image
          iconsCaptured.get(i).setImage(inputImage); // show raw image

          for (int j = 0; j < detectors.size(); j++) { // list of object detectors
            List<Detection> data = detectors.get(j).getObjects(inputImage, displayImage);
            iconsProcessed.get(i * j).setImage(displayImage); // show processed image

            // System.out.println("Got: "+data.length);

          }
          frame.repaint();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /** @param args */
  public static void main(String[] args) { // Main entry for object detection

    new OpenCVTest().run(); // starts
  }
}
