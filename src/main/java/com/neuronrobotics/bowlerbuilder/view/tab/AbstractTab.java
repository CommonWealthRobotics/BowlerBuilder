package com.neuronrobotics.bowlerbuilder.view.tab;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javax.annotation.Nonnull;

/**
 * A generic tab.
 *
 * @param <T> controller type for the content
 */
public abstract class AbstractTab<T> extends Tab {

  public AbstractTab(@Nonnull final String title) {
    super(title);
  }

  /**
   * Get the visual content of this tab.
   *
   * @return root node
   */
  public abstract Node getView();

  /**
   * Get the controller for the content of this tab.
   *
   * @return controller
   */
  public abstract T getController();

}
