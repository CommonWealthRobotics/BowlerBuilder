package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import javafx.scene.control.Accordion;
import javafx.scene.layout.HBox;
import javax.annotation.Nonnull;

public class ConnectionManagerFactory {

  public ConnectionManager get(@Nonnull final HBox connectionsHeader,
      @Nonnull final Accordion accordion) {
    return new ConnectionManager(connectionsHeader, accordion);
  }

}
