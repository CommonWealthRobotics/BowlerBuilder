/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.util.CsgParser;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.shape.MeshView;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

@ParametersAreNonnullByDefault
public class CSGManager {

  private final CsgParser csgParser;
  private final Map<CSG, MeshView> csgMap;
  private final Map<String, MeshView> csgNameMap;

  @Inject
  public CSGManager(final CsgParser csgParser) {
    this.csgParser = csgParser;
    csgMap = new ConcurrentHashMap<>();
    csgNameMap = new ConcurrentHashMap<>();
  }

  public Map<CSG, MeshView> getCsgMap() {
    return csgMap;
  }

  public Map<String, MeshView> getCsgNameMap() {
    return csgNameMap;
  }

  public CsgParser getCsgParser() {
    return csgParser;
  }
}
