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

/**
 * Simple class to hold CSG parsing logic.
 */
public class CsgParser {

  private static final Logger logger = LoggerUtilities.getLogger(CsgParser.class.getSimpleName());

  /**
   * Find the CSG object in the map from the source code.
   *
   * @param scriptName name of CSG source script
   * @param lineNumber line number in script
   * @param csgMap map containing CSG objects
   * @return CSG objects from the script
   */
  public Collection<CSG> parseCsgFromSource(String scriptName,
                                            int lineNumber,
                                            Map<CSG, MeshView> csgMap) {
    Collection<CSG> objsFromScriptLine = new ArrayList<>();

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
            int num = Integer.parseInt(traceParts[1].trim());

            if (num == lineNumber) {
              objsFromScriptLine.add(testCSG);
            }
          } catch (Exception e) {
            logger.log(Level.WARNING,
                "Could not selected CSG in script.\n" + Throwables.getStackTraceAsString(e));
          }
        }));

    return objsFromScriptLine;
  }

}
