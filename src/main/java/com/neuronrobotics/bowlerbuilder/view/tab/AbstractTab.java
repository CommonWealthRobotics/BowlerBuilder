/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.tab;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A generic tab.
 *
 * @param <T> controller type for the content
 */
@ParametersAreNonnullByDefault
public abstract class AbstractTab<T> extends Tab {

  public AbstractTab(final String title) {
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
