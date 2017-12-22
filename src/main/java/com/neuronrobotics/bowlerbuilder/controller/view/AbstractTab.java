package com.neuronrobotics.bowlerbuilder.controller.view;

import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * A generic tab.
 *
 * @param <T> controller type for the content
 */
public abstract class AbstractTab<T> extends Tab {

  public AbstractTab(String title) {
    super(title);
  }

  /**
   * Get the visual content of this tab.
   * @return
   */
  public abstract Node getRoot();

  /**
   * Get the controller for the content of this tab.
   * @return controller
   */
  public abstract T getController();

}
