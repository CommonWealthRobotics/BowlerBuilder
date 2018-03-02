package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.controller.CreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.module.LimbLayoutControllerModule;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.ArrayList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LimbLayoutTest extends AutoClosingApplicationTest {

  private LimbLayoutController controller;

  @Override
  public void start(Stage stage) throws Exception {
    final FXMLLoader loader = new FXMLLoader(CreatureEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new LimbLayoutControllerModule(new MockMobileBase()))::getInstance);

    final Node node = loader.load();
    controller = loader.getController();
    stage.setScene(node.getScene());
    stage.show();
  }

  /*@Test
  void testButtonPresence() {
    assertTrue(lookup("#leg1").tryQuery().isPresent());
  }*/

  private class MockMobileBase extends MobileBase {

    MockMobileBase() {
    }

    private DHParameterKinematics getDH(String name) {
      DHParameterKinematics out = new DHParameterKinematics();
      out.setScriptingName(name);
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getLegs() {
      ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("leg1"));
      out.add(getDH("leg2"));
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getAppendages() {
      ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("arm1"));
      out.add(getDH("arm2"));
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getSteerable() {
      ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("steerable1"));
      out.add(getDH("steerable2"));
      return out;
    }

    @Override
    public ArrayList<DHParameterKinematics> getDrivable() {
      ArrayList<DHParameterKinematics> out = new ArrayList<>();
      out.add(getDH("fixed1"));
      out.add(getDH("fixed2"));
      return out;
    }

  }

}
