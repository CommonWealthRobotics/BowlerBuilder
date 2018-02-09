package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.bowlerbuilder.view.robotmanager.JogWidget;
import javafx.scene.Node;

public class LimbSelection implements Selection {

  @Override
  public Node getWidget() {
    return new JogWidget();
  }

}
