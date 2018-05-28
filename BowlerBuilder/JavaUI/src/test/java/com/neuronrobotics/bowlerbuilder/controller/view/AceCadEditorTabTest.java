/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.util.Verified;
import com.neuronrobotics.bowlerbuilder.view.tab.cadeditor.BaseCadEditorTab;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AceCadEditorTabTest extends AbstractAutoClosingApplicationTest {

  private BaseCadEditorTab tab;

  @Override
  public void start(final Stage stage) throws IOException {
    tab = new BaseCadEditorTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  @Test
  void basicTest() throws ExecutionException, InterruptedException {
    FxHelper.runAndWait(
        () ->
            tab.getController()
                .getDefaultScriptEditorController()
                .getScriptEditor()
                .insertAtCursor("return new Cube(1,1,1).toCSG();"));
    final Object result =
        FxUtil.returnFX(
            () ->
                tab.getController()
                    .getDefaultScriptEditorController()
                    .getScriptRunner()
                    .runScript(
                        tab.getController()
                            .getDefaultScriptEditorController()
                            .getScriptEditor()
                            .getFullText(),
                        new ArrayList<>(),
                        "BowlerGroovy"));

    final Verified verified = (Verified) result;
    assertNotNull(verified);
    assertTrue(verified.getSuccess() instanceof CSG);
  }
}
