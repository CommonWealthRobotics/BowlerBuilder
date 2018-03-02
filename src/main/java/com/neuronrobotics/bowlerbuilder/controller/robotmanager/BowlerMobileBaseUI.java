package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;
import javafx.application.Platform;

public class BowlerMobileBaseUI implements IMobileBaseUI {

  private static final Logger LOGGER
      = LoggerUtilities.getLogger(IMobileBaseUI.class.getSimpleName());

  private final CadEngine cadEngine;

  @Inject
  public BowlerMobileBaseUI(final CadEngine cadEngine) {
    this.cadEngine = cadEngine;
  }

  @Override
  public void setAllCSG(final Collection<CSG> collection, final File file) {
    Platform.runLater(() -> {
      cadEngine.clearMeshes();
      cadEngine.addAllCSGs(collection);
    });
  }

  @Override
  public void addCSG(final Collection<CSG> collection, final File file) {
    Platform.runLater(() -> cadEngine.addAllCSGs(collection));
  }

  @Override
  public void highlightException(final File file, final Exception exception) {
    LOGGER.warning("Exception in CAD script.\n" + Throwables.getStackTraceAsString(exception));
  }

  @Override
  public Set<CSG> getVisibleCSGs() {
    return cadEngine.getCsgMap().keySet();
  }

  @Override
  public void setSelectedCsg(final Collection<CSG> collection) {
    Platform.runLater(() -> cadEngine.selectCSGs(collection));
  }

}
