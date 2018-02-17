package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import javafx.scene.control.Accordion;
import javafx.scene.layout.HBox;

public class ConnectionManagerFactory {

  public ConnectionManager get(HBox connectionsHeader, Accordion accordion) {
    return new ConnectionManager(connectionsHeader, accordion);
  }

}
