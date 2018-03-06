/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
