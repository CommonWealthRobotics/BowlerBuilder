package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerbuilder.view.robotmanager.DHSettingsWidget;
import com.neuronrobotics.bowlerbuilder.view.robotmanager.LinkConfigurationWidget;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ConfigTabLinkSelection extends LinkSelection implements
    EngineeringUnitsChangeListener {

  private final MobileBaseCadManager cadManager;
  private final VBox widget;
  private final DHSettingsWidget dhSettings;
  private final LinkConfigurationWidget linkConfig;

  public ConfigTabLinkSelection(int linkIndex, DHLink dhLink, LinkConfiguration configuration,
      DHParameterKinematics device, MobileBaseCadManager cadManager) {
    super(linkIndex, dhLink, configuration, device);
    this.cadManager = cadManager;

    widget = new VBox(5);
    widget.setPadding(new Insets(5));

    dhSettings = new DHSettingsWidget("D-H Configuration for " + configuration.getName(), dhLink,
        device, this);
    widget.getChildren().add(dhSettings.getView());

    widget.getChildren().add(getTitleLabel("Link Configuration for " + configuration.getName()));
    linkConfig = new LinkConfigurationWidget(configuration, device.getFactory(), null);
    widget.getChildren().add(linkConfig);
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
