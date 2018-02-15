package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class MovementTabLimbSelection extends LimbSelection {

  private final VBox view;

  public MovementTabLimbSelection(DHParameterKinematics limb) {
    super(limb);

    view = new VBox(5);

    view.setPadding(new Insets(5));

    JogWidget jogWidget = new JogWidget(limb);
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
