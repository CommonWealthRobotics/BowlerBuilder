package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import javafx.scene.control.Accordion;
import javafx.scene.layout.HBox;

public class ConnectionManagerFactory {

  public ConnectionManager get(final HBox connectionsHeader, final Accordion accordion) {
    return new ConnectionManager(connectionsHeader, accordion);
  }

}
