package com.neuronrobotics.bowlerbuilder.model.preferences;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * From GRIP. See third-party-licenses/GRIP.txt.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Preference {
  /**
   * The title for this preference. This string gets displayed in the preferences editor UI.
   *
   * @return a name
   */
  String name();

  /**
   * A short description for the purpose of this preference.
   *
   * @return a short description
   */
  String description();
}
