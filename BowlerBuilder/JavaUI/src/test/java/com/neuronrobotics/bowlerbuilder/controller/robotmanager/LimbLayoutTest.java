/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.controller.CreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.module.LimbLayoutControllerModule;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.io.IOException;
import java.util.ArrayList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LimbLayoutTest extends AbstractAutoClosingApplicationTest {

  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader loader =
        new FXMLLoader(
            CreatureEditorController.class.getResource(
                "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"),
            null,
            null,
            BowlerBuilder.getInjector()
                    .createChildInjector(new LimbLayoutControllerModule(new MockMobileBase()))
                ::getInstance);

    final Node node = loader.load();
    stage.setScene(node.getScene());
    stage.show();
  }

  /*@Test
  void testButtonPresence() {
    assertTrue(lookup("#leg1").tryQuery().isPresent());
  }*/

  private static class MockMobileBase extends MobileBase {

    MockMobileBase() { // NOPMD
      // Empty mock ctor
    }

    private DHParameterKinematics getDH(final String name) {
      final DHParameterKinematics out = new DHParameterKinematics();
      out.setScriptingName(name);
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getLegs() {
      final ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("leg1"));
      out.add(getDH("leg2"));
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getAppendages() {
      final ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("arm1"));
      out.add(getDH("arm2"));
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getSteerable() {
      final ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("steerable1"));
      out.add(getDH("steerable2"));
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getDrivable() {
      final ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("fixed1"));
      out.add(getDH("fixed2"));
      return out;
    }
  }
}
