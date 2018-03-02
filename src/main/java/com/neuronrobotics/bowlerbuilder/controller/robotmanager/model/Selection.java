package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javax.annotation.Nonnull;

public interface Selection {

  /**
   * Return the widget that should show when this is current selection.
   *
   * @return widget
   */
  Node getWidget();

  /**
   * Get a Label in a standard font.
   *
   * @param text label text
   * @return label
   */
  default Label getTitleLabel(@Nonnull final String text) {
    final Label label = new Label(text);
    label.setFont(Font.font(16));
    return label;
  }

}
