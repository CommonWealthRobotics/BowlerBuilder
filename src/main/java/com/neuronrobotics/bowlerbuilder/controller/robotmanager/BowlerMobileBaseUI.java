package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BowlerMobileBaseUI implements IMobileBaseUI {

  private static final Logger logger
      = LoggerUtilities.getLogger(IMobileBaseUI.class.getSimpleName());

  private final CadEngine cadEngine;

  public BowlerMobileBaseUI(CadEngine cadEngine) {
    this.cadEngine = cadEngine;
  }

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
    logger.log(Level.WARNING,
        "Exception in CAD script.\n" + Throwables.getStackTraceAsString(e));
  }

  @Override
  public Set<CSG> getVisibleCSGs() {
    return cadEngine.getCsgMap().keySet();
  }

  @Override
  public void setSelectedCsg(Collection<CSG> collection) {
    FxUtil.runFX(() -> cadEngine.selectCSGs(collection));
  }

}
