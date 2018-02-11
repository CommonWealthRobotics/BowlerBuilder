package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class MovementTabLimbSelection extends LimbSelection {

  private final ObjectProperty<LinkConfiguration> selectionProperty;
  private final VBox view;
  private final JogWidget jogWidget;

  public MovementTabLimbSelection(DHParameterKinematics limb) {
    super(limb);

    view = new VBox(5);
    selectionProperty = new SimpleObjectProperty<>();

    view.setPadding(new Insets(5));

    jogWidget = new JogWidget(limb);
    view.getChildren().add(jogWidget.getView());
  }

  @Override
  public Node getWidget() {
    return view;
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
