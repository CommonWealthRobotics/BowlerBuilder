/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.model.preferences;

import java.io.Serializable;

@FunctionalInterface
public interface PreferenceListener<T extends Serializable> {

  /**
   * Called when the preference changes to a new value.
   *
   * @param oldVal old value
   * @param newVal new value
   */
  void changed(T oldVal, T newVal);

}
