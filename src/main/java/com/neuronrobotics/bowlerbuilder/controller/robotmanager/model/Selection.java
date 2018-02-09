package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import javafx.scene.Node;

public interface Selection {

  /**
   * Return the widget that should show when this is current selection.
   *
   * @return widget
   */
  Node getWidget();

}
