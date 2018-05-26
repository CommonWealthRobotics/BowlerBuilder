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
package com.neuronrobotics.bowlerstudio.assets;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

public class AssetFactory {

  private static final String repo = "BowlerStudioImageAssets";
  private static String gitSource = "https://github.com/madhephaestus/" + repo + ".git";
  private static HashMap<String, Image> cache = new HashMap<>();
  private static HashMap<String, FXMLLoader> loaders = new HashMap<>();
  private static String assetRepoBranch = "";

  private AssetFactory() {}

  private static FXMLLoader loadLayout(final String file, final boolean refresh) throws Exception {
    final File fxmlFIle = loadFile(file);
    final URL fileURL = fxmlFIle.toURI().toURL();

    if (loaders.get(file) == null || refresh) {
      loaders.put(file, new FXMLLoader(fileURL));
    }

    loaders.get(file).setLocation(fileURL);
    return loaders.get(file);
  }

  private static FXMLLoader loadLayout(final String file) throws Exception {
    return loadLayout(file, false);
  }

  private static File loadFile(final String file) throws Exception {
    return ScriptingEngine.fileFromGit(
        getGitSource(), // git repo, change this if you fork this demo
        getAssetRepoBranch(),
        file // File from within the Git repo
        );
  }

  @SuppressWarnings("restriction")
  public static Image loadAsset(final String file) throws Exception {
    if (cache.get(file) == null) {
      final File f = loadFile(file);
      if (f.getName().endsWith(".fxml")) {
        loadLayout(file);
        return null;
      } else if ((f == null || !f.exists()) && f.getName().endsWith(".png")) {
        final WritableImage obj_img = new WritableImage(30, 30);
        final byte alpha = (byte) 0;
        for (int cx = 0; cx < obj_img.getWidth(); cx++) {
          for (int cy = 0; cy < obj_img.getHeight(); cy++) {
            final int color = obj_img.getPixelReader().getArgb(cx, cy);
            final int mc = (alpha << 24) | 0x00ffffff;
            final int newColor = color & mc;
            obj_img.getPixelWriter().setArgb(cx, cy, newColor);
          }
        }

        cache.put(file, obj_img);
        System.out.println("No image at " + file);

        try {
          final File imageFile = ScriptingEngine.createFile(getGitSource(), file, "create file");
          try {
            final String fileName = imageFile.getName();
            ImageIO.write(
                SwingFXUtils.fromFXImage(obj_img, null),
                fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(),
                imageFile);

          } catch (final IOException ignored) {
          }
          ScriptingEngine.createFile(getGitSource(), file, "saving new content");
        } catch (final Exception e) {
          e.printStackTrace();
        }
      } else {
        cache.put(file, new Image(f.toURI().toString()));
      }
    }
    return cache.get(file);
  }

  public static ImageView loadIcon(final String file) {
    try {
      return new ImageView(loadAsset(file));
    } catch (final Exception e) {
      return new ImageView();
    }
  }

  public static String getGitSource() {
    return gitSource;
  }

  public static void setGitSource(final String gitSource, final String assetRepoBranch) throws Exception {
    System.err.println("Assets from: " + gitSource + "#" + assetRepoBranch);
    // new Exception().printStackTrace();
    setAssetRepoBranch(assetRepoBranch);
    AssetFactory.gitSource = gitSource;
    cache.clear();
    loadAllAssets();
  }

  private static void loadAllAssets() throws Exception {
    final List<String> files = ScriptingEngine.filesInGit(gitSource, StudioBuildInfo.getVersion(), null);
    for (final String file : files) {
      loadAsset(file);
    }
  }

  private static String getAssetRepoBranch() {
    return assetRepoBranch;
  }

  private static void setAssetRepoBranch(final String assetRepoBranch) {
    AssetFactory.assetRepoBranch = assetRepoBranch;
  }

  private static void deleteFolder(final File folder) {
    final File[] files = folder.listFiles();
    if (files != null) { // some JVMs return null for empty dirs
      for (final File f : files) {
        if (f.isDirectory()) {
          deleteFolder(f);
        } else {
          f.delete();
        }
      }
    }
    folder.delete();
  }
}
