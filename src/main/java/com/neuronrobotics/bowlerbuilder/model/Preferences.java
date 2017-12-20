package com.neuronrobotics.bowlerbuilder.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.Property;

/**
 * Entire application preferences.
 */
public class Preferences {

  private final Map<String, Property> props;

  /**
   * Make a new Preferences with empty initial preferences.
   */
  public Preferences() {
    props = new ConcurrentHashMap<>();
  }

  /**
   * Make a new Preferences with some initial preferences.
   *
   * @param initialPreferences initial preferences, usually loaded from disk
   */
  public Preferences(Map<String, Property> initialPreferences) {
    props = new ConcurrentHashMap<>(initialPreferences);
  }

  /**
   * Get the Property associated with a name.
   *
   * @param query property key
   * @return mapped property
   */
  public Property get(String query) {
    return props.get(query);
  }

  /**
   * Get a collection of all Properties.
   *
   * @return all properties
   */
  public Collection<Property> getAllProperties() {
    return props.values();
  }

}
