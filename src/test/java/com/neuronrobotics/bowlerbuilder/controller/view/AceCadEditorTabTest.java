/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.view;

import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.view.tab.AceCadEditorTab;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AceCadEditorTabTest extends AutoClosingApplicationTest {

  private AceCadEditorTab tab;

  @Override
  public void start(Stage stage) throws Exception {
    tab = new AceCadEditorTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  @Test
  void basicTest() throws Exception {
    FxUtil.runFX(() ->
        tab.getController().getScriptEditor().insertAtCursor("return new Cube(1,1,1).toCSG();"));
    Object result = FxUtil.returnFX(() -> tab.getController().getScriptRunner().runScript(
        tab.getController().getScriptEditor().getText(),
        new ArrayList<>(),
        "BowlerGroovy"
    ));

    assertTrue(result instanceof CSG);
  }

}
