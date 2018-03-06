/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.cadengine.util;

import static org.junit.Assert.assertEquals;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.shape.MeshView;
import org.junit.jupiter.api.Test;

public class CsgParserTest {

  @Test
  public void parseCsgTest() throws Exception {
    String script = "CSG foo = new Cube(1,1,1).toCSG();";
    CSG result = (CSG) ScriptingEngine.inlineScriptStringRun(
        script,
        new ArrayList<>(),
        "Groovy");

    Map<CSG, MeshView> csgMap = new HashMap<>();
    csgMap.put(result, result.getMesh());

    CsgParser parser = new CsgParser();
    Collection<CSG> test = parser.parseCsgFromSource("Script1", 1, csgMap);

    assertEquals(result, test.iterator().next());
  }

}
