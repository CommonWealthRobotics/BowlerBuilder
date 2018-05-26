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
package com.neuronrobotics.bowlerstudio.creature;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.svg.SVGExporter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;

public class CadFileExporter {

  IMobileBaseUI ui;

  public CadFileExporter(IMobileBaseUI myUI) {
    ui = myUI;
  }

  public CadFileExporter() {
    ui =
        new IMobileBaseUI() {

          @Override
          public void setSelectedCsg(Collection<CSG> selectedCsg) {
            // TODO Auto-generated method stub

          }

          @Override
          public void setAllCSG(Collection<CSG> toAdd, File source) {
            // TODO Auto-generated method stub

          }

          @Override
          public void highlightException(File fileEngineRunByName, Exception ex) {
            // TODO Auto-generated method stub

          }

          @Override
          public Set<CSG> getVisibleCSGs() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public void addCSG(Collection<CSG> toAdd, File source) {
            // TODO Auto-generated method stub

          }
        };
  }

  public ArrayList<File> generateManufacturingParts(List<CSG> totalAssembly, File baseDirForFiles)
      throws IOException {
    ArrayList<File> allCadStl = new ArrayList<>();
    if (!baseDirForFiles.isDirectory()) {
      String fileNameWithOutExt = FilenameUtils.removeExtension(baseDirForFiles.getAbsolutePath());
      baseDirForFiles = new File(fileNameWithOutExt);
      if (!baseDirForFiles.exists()) {
        baseDirForFiles.mkdirs();
      }
    }
    File dir;
    if (!baseDirForFiles.getName().contentEquals("manufacturing")) {
      dir = new File(baseDirForFiles.getAbsolutePath() + "/manufacturing/");
      if (!dir.exists()) {
        dir.mkdirs();
      }
    } else {
      dir = baseDirForFiles;
    }
    int index = 0;
    ArrayList<CSG> svgParts = new ArrayList<>();
    String svgName = null;
    String nameBase = "";
    for (CSG part : totalAssembly) {
      String name = part.getName();
      CSG manufactured = part.prepForManufacturing();
      if (manufactured == null) {
        continue;
      }
      manufactured.setName(part.getName());
      nameBase = dir.getAbsolutePath() + "/" + index + "-" + name;
      index++;
      if (part.getExportFormats() == null) {
        allCadStl.add(makeStl(nameBase, manufactured)); // default to stl
      } else {

        for (String format : part.getExportFormats()) {

          if (format.toLowerCase().contains("stl")) {
            allCadStl.add(makeStl(nameBase, manufactured)); // default to stl
            ui.setCsg(manufactured, null);
          }
          if (format.toLowerCase().contains("svg")) {
            if (svgName == null) {
              svgName = part.toString();
            }
            svgParts.add(manufactured);
            ui.setAllCSG(svgParts, null);
          }
        }
      }
    }
    if (svgParts.size() > 0) {
      allCadStl.add(makeSvg(nameBase, svgParts)); // default to stl
    }

    return allCadStl;
  }

  private File makeStl(String nameBase, CSG tmp) throws IOException {
    File stl = new File(nameBase + ".stl");

    FileUtil.write(Paths.get(stl.getAbsolutePath()), tmp.toStlString());
    System.out.println("Writing " + stl.getAbsolutePath());
    return stl;
  }

  private File makeSvg(String nameBase, List<CSG> currentCsg) throws IOException {
    File stl = new File(nameBase + ".svg");

    for (CSG csg : currentCsg) {
      if (csg.getSlicePlanes() == null) {
        csg.addSlicePlane(new Transform());
      }
    }
    try {
      SVGExporter.export(currentCsg, stl);
    } catch (Exception e) {
      ArrayList<CSG> movedDown = new ArrayList<>();
      for (CSG csg : currentCsg) {
        CSG movez = csg.toZMin().movez(-0.01);
        if (movez.getSlicePlanes() == null) {
          movez.addSlicePlane(new Transform());
        }
        movez.setName(csg.getName());
        movedDown.add(movez);
      }
      SVGExporter.export(movedDown, stl);
    }

    System.out.println("Writing " + stl.getAbsolutePath());
    return stl;
  }
}
