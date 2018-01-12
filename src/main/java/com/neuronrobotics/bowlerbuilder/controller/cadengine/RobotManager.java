package com.neuronrobotics.bowlerbuilder.controller.cadengine;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

public class RobotManager {

  private static final Logger logger
      = LoggerUtilities.getLogger(RobotManager.class.getSimpleName());
  private final CadEngine cadEngine;

  public RobotManager(CadEngine cadEngine) {
    this.cadEngine = cadEngine;

    try {
      IMobileBaseUI mobileBaseUI = new IMobileBaseUI() {
        @Override
        public void setAllCSG(Collection<CSG> collection, File file) {
          FxUtil.runFX(() -> {
            cadEngine.clearMeshes();
            cadEngine.addAllCSGs(collection);
          });
        }

        @Override
        public void addCSG(Collection<CSG> collection, File file) {
          FxUtil.runFX(() -> cadEngine.addAllCSGs(collection));
        }

        @Override
        public void highlightException(File file, Exception e) {
          e.printStackTrace();
        }

        @Override
        public Set<CSG> getVisibleCSGs() {
          return cadEngine.getCsgMap().keySet();
        }

        @Override
        public void setSelectedCsg(Collection<CSG> collection) {
          FxUtil.runFX(() -> cadEngine.selectCSGs(collection));
        }
      };

      String[] file = {"https://github.com/madhephaestus/SeriesElasticActuator.git",
          "seaArm.xml"};
      String xmlContent = ScriptingEngine.codeFromGit(file[0], file[1])[0];
      MobileBase mobileBase = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));
      mobileBase.setGitSelfSource(file);
      mobileBase.connect();
      MobileBaseCadManager mobileBaseCadManager = new MobileBaseCadManager(
          mobileBase, mobileBaseUI);
      DeviceManager.addConnection(mobileBase, mobileBase.getScriptingName());
      mobileBaseCadManager.generateCad();
      System.out.println("\nWaiting for cad to generate");
      ThreadUtil.wait(1000);
      while (MobileBaseCadManager.get(mobileBase).getProcesIndictor().get() < 1) {
        ThreadUtil.wait(1000);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE,
          "Could not load assets for robot.\n" + Throwables.getStackTraceAsString(e));
    } catch (Exception e) {
      logger.log(Level.SEVERE,
          "Could not start building robot.\n" + Throwables.getStackTraceAsString(e));
    }
  }

}
