/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.cadengine.util;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.shape.MeshView;
import javax.annotation.Nonnull;

/**
 * Simple class to hold CSG parsing logic.
 */
public class CsgParser {

  private static final Logger LOGGER = LoggerUtilities.getLogger(CsgParser.class.getSimpleName());

  /**
   * Find the CSG object in the map from the source code.
   *
   * @param scriptName name of CSG source script
   * @param lineNumber line number in script
   * @param csgMap map containing CSG objects
   * @return CSG objects from the script
   */
  public Collection<CSG> parseCsgFromSource(@Nonnull final String scriptName,
      final int lineNumber,
      @Nonnull final Map<CSG, MeshView> csgMap) {
    final Collection<CSG> objsFromScriptLine = new ArrayList<>();

    csgMap.keySet().forEach(testCSG -> testCSG.getCreationEventStackTraceList().stream()
        .map(trace -> trace.split(":"))
        .filter(traceParts ->
            traceParts[0].trim()
                .toLowerCase(Locale.US)
                .contains(scriptName
                    .toLowerCase(Locale.US)
                    .trim()))
        .forEach(traceParts -> {
          try {
            final int num = Integer.parseInt(traceParts[1].trim());

            if (num == lineNumber) {
              objsFromScriptLine.add(testCSG);
            }
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING,
                "Could not selected CSG in script.\n" + Throwables.getStackTraceAsString(e));
          }
        }));

    return objsFromScriptLine;
  }

}
