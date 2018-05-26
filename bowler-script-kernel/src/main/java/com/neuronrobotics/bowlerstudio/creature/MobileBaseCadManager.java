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

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.util.FileWatchDeviceWrapper;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.IFileChangeListener;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class MobileBaseCadManager {

  // static
  private static HashMap<MobileBase, MobileBaseCadManager> cadmap = new HashMap<>();
  // static
  private Object cadEngine;
  private MobileBase base;
  private File cadScript;

  private HashMap<DHParameterKinematics, Object> dhCadGen = new HashMap<>();
  private HashMap<DHParameterKinematics, ArrayList<CSG>> DHtoCadMap = new HashMap<>();
  private HashMap<LinkConfiguration, ArrayList<CSG>> LinktoCadMap = new HashMap<>();
  private HashMap<MobileBase, ArrayList<CSG>> BasetoCadMap = new HashMap<>();

  private boolean cadGenerating = false;
  private boolean showingStl = false;
  private ArrayList<CSG> allCad;

  private boolean bail = false;
  private IMobileBaseUI ui = null;
  private IFileChangeListener cadWatcher =
      new IFileChangeListener() {

        @Override
        public void onFileChange(final File fileThatChanged, final WatchEvent event) {

          if (cadGenerating || !getAutoRegen()) {
            System.out.println("No Base reload, building currently");
            return;
          }
          try {
            new Thread(() -> {
              ThreadUtil.wait((int) ((50 * Math.random()) + 50));
              try {

                System.out.println("Re-loading Cad Base Engine");
                cadEngine = ScriptingEngine.inlineFileScriptRun(fileThatChanged, null);
              } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              generateCad();
            }).start();
          } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      };
  private boolean autoRegen = true;
  private DoubleProperty pi = new SimpleDoubleProperty(0);

  public MobileBaseCadManager(final MobileBase base, final IMobileBaseUI myUI) {
    this.setUi(myUI);
    base.addConnectionEventListener(
        new IDeviceConnectionEventListener() {

          @Override
          public void onDisconnect(final BowlerAbstractDevice arg0) {
            bail = true;
            dhCadGen.clear();
            DHtoCadMap.clear();
            LinktoCadMap.clear();
            BasetoCadMap.clear();
            cadmap.remove(get(base));
          }

          @Override
          public void onConnect(final BowlerAbstractDevice arg0) {
            // TODO Auto-generated method stub

          }
        });
    setMobileBase(base);

    // new Exception().printStackTrace();
  }

  private File getCadScript() {
    return cadScript;
  }

  private void setCadScript(final File cadScript) {
    if (cadScript == null) {
      return;
    }
    FileWatchDeviceWrapper.watch(base, cadScript, cadWatcher);

    this.cadScript = cadScript;
  }

  private IgenerateBody getIgenerateBody() {
    if (IgenerateBody.class.isInstance(cadEngine)) {
      return (IgenerateBody) cadEngine;
    }
    return null;
  }

  private IgenerateCad getIgenerateCad() {
    if (IgenerateCad.class.isInstance(cadEngine)) {
      return (IgenerateCad) cadEngine;
    }
    return null;
  }

  private IgenerateBed getIgenerateBed() {
    if (IgenerateBed.class.isInstance(cadEngine)) {
      return (IgenerateBed) cadEngine;
    }
    return null;
  }

  private ArrayList<CSG> generateBody(final MobileBase base) {

    getProcesIndictor().set(0);
    setAllCad(new ArrayList<>());
    // DHtoCadMap = new HashMap<>();
    // private HashMap<MobileBase, ArrayList<CSG>> BasetoCadMap = new
    // HashMap<>();

    getBasetoCadMap().computeIfAbsent(base, k -> new ArrayList<>());

    if (cadEngine == null) {
      try {
        setDefaultLinkLevelCadEngine();
      } catch (final Exception e) {
        getUi().highlightException(null, e);
      }
      if (getCadScript() != null) {
        try {
          cadEngine = ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
        } catch (final Exception e) {
          getUi().highlightException(getCadScript(), e);
        }
      }
    }
    getProcesIndictor().set(0.1);
    try {
      getAllCad().clear();
      if (showingStl) {
        // skip the regen
        for (final CSG c : getBasetoCadMap().get(base)) {
          getAllCad().add(c);
        }
      } else {
        if (!bail) {
          final ArrayList<CSG> newcad = getIgenerateBody().generateBody(base);
          for (final CSG c : newcad) {
            getAllCad().add(c);
          }
          ui.addCSG(newcad, getCadScript());
        } else {
          new Exception().printStackTrace();
        }
        final ArrayList<CSG> arrayList = getBasetoCadMap().get(base);
        arrayList.clear();
        for (final CSG c : getAllCad()) {
          arrayList.add(c);
        }
        new Thread(
                () -> {
                  localGetBaseCad(base); // load the cad union in a thread to
                  // make it ready for physics
                })
            .start();
      }
    } catch (final Exception e) {
      getUi().highlightException(getCadScript(), e);
    }
    System.out.println("Displaying Body");
    getProcesIndictor().set(0.35);
    // clears old robot and places base
    getUi().setAllCSG(getBasetoCadMap().get(base), getCadScript());
    System.out.println("Rendering limbs");
    getProcesIndictor().set(0.4);
    final ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
    final double numLimbs = limbs.size();
    int i = 0;
    for (final DHParameterKinematics l : limbs) {
      getDHtoCadMap().computeIfAbsent(l, k -> new ArrayList<>());
      final ArrayList<CSG> arrayList = getDHtoCadMap().get(l);
      int j = 0;
      if (showingStl || !base.isAvailable()) {
        for (final CSG csg : arrayList) {
          getAllCad().add(csg);
          getUi().addCsg(csg, getCadScript());
          set(base, i, j);
          j += 1;
        }
      } else {

        arrayList.clear();
        final ArrayList<CSG> linksCad = generateCad(l);

        for (final CSG csg : linksCad) {

          getAllCad().add(csg);
          arrayList.add(csg);
          getUi().addCsg(csg, getCadScript());
          j += 1;
        }
      }

      i += 1;
    }

    showingStl = false;
    getProcesIndictor().set(1);
    // PhysicsEngine.clear();
    // MobileBasePhysicsManager m = new MobileBasePhysicsManager(base,
    // baseCad, getSimplecad());
    // PhysicsEngine.startPhysicsThread(50);
    // return PhysicsEngine.getCsgFromEngine();
    return getAllCad();
  }

  private void set(final MobileBase base, final int limb, final int link) {
    final ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
    final double numLimbs = limbs.size();
    final DHParameterKinematics dh = limbs.get(limb);
    final double partsTotal = numLimbs * dh.getNumberOfLinks();
    final double progress = ((double) ((limb * dh.getNumberOfLinks()) + link)) / partsTotal;
    System.out.println(
        "Cad progress "
            + progress
            + " limb "
            + limb
            + " link "
            + link
            + " total parts "
            + partsTotal);
    getProcesIndictor().set(0.333 + (2 * (progress / 3)));
  }

  private LinkConfiguration getLinkConfiguration(final CSG cad) {
    LinkConfiguration conf = null;
    for (final LinkConfiguration c : LinktoCadMap.keySet()) {
      for (final CSG cadTest : LinktoCadMap.get(c)) {
        if (cadTest == cad) {
          conf = c;
        }
      }
    }
    return conf;
  }

  public ArrayList<File> generateStls(
      final MobileBase base, final File baseDirForFiles, final boolean kinematic)
      throws IOException {
    final IgenerateBed bed = getIgenerateBed();
    if (bed == null || kinematic) {
      return _generateStls(base, baseDirForFiles, kinematic);
    }
    System.out.println("Found arrangeBed API in CAD engine");
    final List<CSG> totalAssembly = bed.arrangeBed(base);
    getUi().setAllCSG(totalAssembly, getCadScript());
    final File dir = new File(baseDirForFiles.getAbsolutePath() + "/" + base.getScriptingName());
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return new CadFileExporter(getUi()).generateManufacturingParts(totalAssembly, baseDirForFiles);
  }

  private ArrayList<File> _generateStls(
      final MobileBase base, final File baseDirForFiles, final boolean kinematic) {
    final ArrayList<File> allCadStl = new ArrayList<>();
    final ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
    final double numLimbs = limbs.size();
    int i;
    // Start by generating the legs using the DH link based generator
    final ArrayList<CSG> totalAssembly = new ArrayList<>();
    final double offset = 0;
    for (i = 0; i < limbs.size(); i += 1) {

      final double progress = (1.0 - ((numLimbs - i) / numLimbs)) / 2;
      getProcesIndictor().set(progress);

      final DHParameterKinematics l = limbs.get(i);
      final ArrayList<CSG> parts = getDHtoCadMap().get(l);
      for (int j = 0; j < parts.size(); j++) {
        CSG csg = parts.get(j);
        final String name = csg.getName();
        try {
          final CSG tmp;
          if (!kinematic) {
            csg = csg.prepForManufacturing();
          }
          if (csg != null) {
            if (!kinematic) {
              tmp = csg.toXMax().toYMax();
            } else {
              tmp = csg;
            }
            if (totalAssembly.size() > 0 && !kinematic) {
              totalAssembly.add(
                  tmp.movey(
                      .5
                          + totalAssembly.get(totalAssembly.size() - 1).getMaxY()
                          + Math.abs(csg.getMinY())));
            } else {
              totalAssembly.add(tmp);
            }
            final LinkConfiguration conf = getLinkConfiguration(parts.get(j));

            final String linkNum = conf.getLinkIndex() + "_Link_";

            final File dir =
                new File(
                    baseDirForFiles.getAbsolutePath()
                        + "/"
                        + base.getScriptingName()
                        + "/"
                        + l.getScriptingName());
            if (!dir.exists()) {
              dir.mkdirs();
            }
            System.out.println("Making STL for " + name);
            final File stl =
                new File(
                    dir.getAbsolutePath()
                        + "/"
                        + linkNum
                        + name
                        + "_limb_"
                        + i
                        + "_Part_"
                        + j
                        + ".stl");
            FileUtil.write(Paths.get(stl.getAbsolutePath()), tmp.toStlString());
            allCadStl.add(stl);
            // totalAssembly.add(tmp);
            getUi().setAllCSG(totalAssembly, getCadScript());
            set(base, i, j);
          }
        } catch (final Exception ex) {
          getUi().highlightException(getCadScript(), ex);
        }
        // legAssembly.setManufactuing(new PrepForManufacturing() {
        // public CSG prep(CSG arg0) {
        // return null;
        // }
        // });
      }
      // offset =
      // -2-((legAssembly.get(legAssembly.size()-1).getMaxX()+legAssembly.get(legAssembly.size()-1).getMinX())*i);
      // legAssembly=legAssembly.movex(offset);

    }

    int link = 0;
    // now we genrate the base pieces
    for (CSG csg : getBasetoCadMap().get(base)) {
      final String name = csg.getName();
      try {
        if (!kinematic) {
          csg = csg.prepForManufacturing();
        }
        if (csg != null) {
          if (!kinematic) {
            csg = csg.toYMin().movex(-2 - csg.getMaxX() + offset);
          }
          final File dir =
              new File(baseDirForFiles.getAbsolutePath() + "/" + base.getScriptingName() + "/");
          if (!dir.exists()) {
            dir.mkdirs();
          }
          final File stl = new File(dir.getAbsolutePath() + "/" + name + "_Body_part_" + link + ".stl");
          FileUtil.write(Paths.get(stl.getAbsolutePath()), csg.toStlString());
          allCadStl.add(stl);
          totalAssembly.add(csg);
          getUi().setAllCSG(totalAssembly, getCadScript());
          link++;
        }
      } catch (final Exception ex) {
        getUi().highlightException(getCadScript(), ex);
      }
    }
    // ui.setCsg(BasetoCadMap.get(base),getCadScript());
    // for(CSG c: DHtoCadMap.get(base.getAllDHChains().get(0))){
    // ui.addCsg(c,getCadScript());
    // }
    showingStl = true;
    getProcesIndictor().set(1);
    return allCadStl;
  }

  private MobileBase getMobileBase() {
    return base;
  }

  private void setMobileBase(final MobileBase base) {
    this.base = base;
    cadmap.put(base, this);
    MobileBaseLoader.get(base); // load the dependant scripts
    base.updatePositions();
  }

  /** This function iterates through the links generating them */
  private ArrayList<CSG> generateCad(final DHParameterKinematics dh) {
    final ArrayList<CSG> dhLinks = new ArrayList<>();

    if (cadEngine == null) {
      try {
        setDefaultLinkLevelCadEngine();
      } catch (final Exception e) {
        getUi().highlightException(getCadScript(), e);
      }
    }

    try {
      IgenerateCad generatorToUse = getIgenerateCad();
      if (dhCadGen.get(dh) != null) {
        final Object object = dhCadGen.get(dh);
        if (IgenerateCad.class.isInstance(object)) {
          generatorToUse = (IgenerateCad) object;
        }
      }
      int j = 0;
      for (final DHParameterKinematics dhtest : getMobileBase().getAllDHChains()) {
        if (dhtest == dh) {
          break;
        }
        j++;
      }
      for (int i = 0; i < dh.getNumberOfLinks(); i++) {
        set(base, j, i);

        if (!bail) {
          final ArrayList<CSG> tmp = generatorToUse.generateCad(dh, i);
          getUi().addCSG(tmp, getCadScript());
          final LinkConfiguration configuration = dh.getLinkConfiguration(i);
          if (getLinktoCadMap().get(configuration) == null) {
            getLinktoCadMap().put(configuration, new ArrayList<>());
          } else {
            getLinktoCadMap().get(configuration).clear();
          }
          for (final CSG c : tmp) {
            dhLinks.add(c);
            getLinktoCadMap().get(configuration).add(c); // add to
            // the
            // regestration
            // storage
          }
          final AbstractLink link = dh.getFactory().getLink(configuration);
          link.addLinkListener(
              new ILinkListener() {

                @Override
                public void onLinkPositionUpdate(final AbstractLink arg0, final double arg1) {
                  // TODO Auto-generated method stub

                }

                @Override
                public void onLinkLimit(final AbstractLink arg0, final PIDLimitEvent arg1) {
                  selectCsgByLink(base, configuration);
                }
              });
        }
      }
      return dhLinks;
    } catch (final Exception e) {
      getUi().highlightException(getCadScript(), e);
    }
    return null;
  }

  public void selectCsgByMobileBase(final MobileBase base) {
    try {

      final ArrayList<CSG> csg = MobileBaseCadManager.get(base).getBasetoCadMap().get(base);
      getUi().setSelectedCsg(csg);
    } catch (final Exception ex) {
      System.err.println("Base not loaded yet");
    }
  }

  public void selectCsgByLimb(final MobileBase base, final DHParameterKinematics limb) {
    try {

      final ArrayList<CSG> limCad = MobileBaseCadManager.get(base).getDHtoCadMap().get(limb);

      getUi().setSelectedCsg(limCad);
    } catch (final Exception ex) {
      System.err.println("Limb not loaded yet");
    }
  }

  private void selectCsgByLink(final MobileBase base, final LinkConfiguration limb) {
    try {

      final ArrayList<CSG> limCad = MobileBaseCadManager.get(base).getLinktoCadMap().get(limb);
      getUi().setSelectedCsg(limCad);
    } catch (final Exception ex) {
      System.err.println("Limb not loaded yet");
    }
  }

  public void generateCad() {
    if (cadGenerating || !getAutoRegen()) {
      return;
    }
    cadGenerating = true;
    // new RuntimeException().printStackTrace();
    // new Exception().printStackTrace();
    new Thread() {
      @Override
      public void run() {
        System.out.print("\r\nGenerating CAD...\r\n");
        setName("MobileBaseCadManager Generating cad Thread ");
        // new Exception().printStackTrace();
        final MobileBase device = base;
        try {
          setAllCad(generateBody(device));
        } catch (final Exception e) {
          getUi().highlightException(getCadScript(), e);
        }
        // System.out.print("\r\nDone Generating CAD!\r\n");
        getUi().setCsg(MobileBaseCadManager.get(base), getCadScript());
        cadGenerating = false;
        System.out.print("\r\nDone Generating CAD!\r\n");
        getProcesIndictor().set(1);
      }
    }.start();
  }

  private void setDefaultLinkLevelCadEngine() throws Exception {
    final String[] cad;
    cad = base.getGitCadEngine();

    if (cadEngine == null) {
      setGitCadEngine(cad[0], cad[1], base);
    }
    for (final DHParameterKinematics kin : base.getAllDHChains()) {
      final String[] kinEng = kin.getGitCadEngine();
      if (!cad[0].contentEquals(kinEng[0]) || !cad[1].contentEquals(kinEng[1])) {
        setGitCadEngine(kinEng[0], kinEng[1], kin);
      }
    }
  }

  public void onTabClosing() {}

  private void setGitCadEngine(final String gitsId, final String file,
      final DHParameterKinematics dh)
      throws GitAPIException, IOException {
    dh.setGitCadEngine(new String[] {gitsId, file});
    final File code = ScriptingEngine.fileFromGit(gitsId, file);
    try {
      final Object defaultDHSolver = ScriptingEngine.inlineFileScriptRun(code, null);
      dhCadGen.put(dh, defaultDHSolver);
    } catch (final Exception e) {
      getUi().highlightException(code, e);
    }

    FileWatchDeviceWrapper.watch(
        dh,
        code,
        (fileThatChanged, event) -> {
          System.out.println("Re-loading Cad Limb Engine");

          try {
            final Object d = ScriptingEngine.inlineFileScriptRun(code, null);
            dhCadGen.put(dh, d);
            generateCad();
          } catch (final Exception ex) {
            getUi().highlightException(code, ex);
          }
        });
  }

  private void setGitCadEngine(final String gitsId, final String file, final MobileBase device)
      throws GitAPIException, IOException {
    setCadScript(ScriptingEngine.fileFromGit(gitsId, file));
    device.setGitCadEngine(new String[] {gitsId, file});
  }

  public ArrayList<CSG> getAllCad() {
    return allCad;
  }

  private void setAllCad(final ArrayList<CSG> allCad) {
    for (final CSG part : allCad) {
      for (final String p : part.getParameters()) {
        CSGDatabase.addParameterListener(
            p,
            (arg0, arg1) -> {
              // generateCad(); //TODO Undo this after debugging
            });
      }
    }
    this.allCad = allCad;
  }

  public static MobileBaseCadManager get(final MobileBase device) {
    if (cadmap.get(device) == null) {
      // new RuntimeException("No Mobile Base Cad Manager UI specified").printStackTrace();
      final MobileBaseCadManager mbcm =
          new MobileBaseCadManager(
              device,
              new IMobileBaseUI() {

                private ArrayList<CSG> list = new ArrayList<>();

                @Override
                public void highlightException(final File fileEngineRunByName, final Exception ex) {
                  ex.printStackTrace();
                }

                @Override
                public void setAllCSG(final Collection<CSG> toAdd, final File source) {
                  // TODO Auto-generated method stub
                  // TODO Auto-generated method stub
                  list.clear();
                  list.addAll(toAdd);
                }

                @Override
                public void addCSG(final Collection<CSG> toAdd, final File source) {
                  // TODO Auto-generated method stub
                  list.addAll(toAdd);
                }

                @Override
                public Set<CSG> getVisibleCSGs() {
                  // TODO Auto-generated method stub
                  return new HashSet<>(list);
                }

                @Override
                public void setSelectedCsg(final Collection<CSG> selectedCsg) {
                  // TODO Auto-generated method stub

                }
              });
      cadmap.put(device, mbcm);
    }
    return cadmap.get(device);
  }

  public static HashMap<LinkConfiguration, ArrayList<CSG>> getSimplecad(final MobileBase device) {
    return get(device).LinktoCadMap;
  }

  private ArrayList<CSG> localGetBaseCad(final MobileBase device) {

    return BasetoCadMap.get(device);
  }

  public static ArrayList<CSG> getBaseCad(final MobileBase device) {
    return get(device).localGetBaseCad(device);
  }

  public DoubleProperty getProcesIndictor() {
    return pi;
  }

  public void setProcesIndictor(final DoubleProperty pi) {
    this.pi = pi;
  }

  private HashMap<MobileBase, ArrayList<CSG>> getBasetoCadMap() {
    return BasetoCadMap;
  }

  public void setBasetoCadMap(final HashMap<MobileBase, ArrayList<CSG>> basetoCadMap) {
    BasetoCadMap = basetoCadMap;
  }

  private HashMap<DHParameterKinematics, ArrayList<CSG>> getDHtoCadMap() {
    return DHtoCadMap;
  }

  public void setDHtoCadMap(final HashMap<DHParameterKinematics, ArrayList<CSG>> dHtoCadMap) {
    DHtoCadMap = dHtoCadMap;
  }

  private HashMap<LinkConfiguration, ArrayList<CSG>> getLinktoCadMap() {
    return LinktoCadMap;
  }

  public void setLinktoCadMap(final HashMap<LinkConfiguration, ArrayList<CSG>> linktoCadMap) {
    LinktoCadMap = linktoCadMap;
  }

  private boolean getAutoRegen() {
    return autoRegen;
  }

  public void setAutoRegen(final boolean autoRegen) {
    this.autoRegen = autoRegen;
  }

  public IMobileBaseUI getUi() {
    return ui;
  }

  private void setUi(final IMobileBaseUI ui) {
    this.ui = ui;
  }
}
