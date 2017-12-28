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
