package com.neuronrobotics.bowlerbuilder.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.Property;

public class Preferences {

  private final Map<String, Property> props;

  public Preferences(Map<String, Property> initialPreferences) {
    props = new ConcurrentHashMap<>(initialPreferences);
  }

  public Property get(String query) {
    return props.get(query);
  }

  public Collection<Property> getAllProperties() {
    return props.values();
  }

}
