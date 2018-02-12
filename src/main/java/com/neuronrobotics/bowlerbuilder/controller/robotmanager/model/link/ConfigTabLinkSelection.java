package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.view.robotmanager.DHSettingsWidget;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;

public class ConfigTabLinkSelection extends LinkSelection implements
    EngineeringUnitsChangeListener {

  private final MobileBaseCadManager cadManager;
  private final DHSettingsWidget widget;

  public ConfigTabLinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      DHParameterKinematics device, MobileBaseCadManager cadManager) {
    super(linkIndex, dhLink, configuration, device);
    this.cadManager = cadManager;

    widget = new DHSettingsWidget("D-H Configuration for " + configuration.getName(), dhLink,
        device, this);
  }

  @Override
  public Node getWidget() {
    return widget;
  }

  @Override
  public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {

  }

  @Override
  public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    cadManager.generateCad();
  }

}
