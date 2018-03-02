package com.neuronrobotics.bowlerbuilder.model.preferences;

import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PreferencesServiceFactory {

  private static final Map<String, PreferencesService> PREFERENCES_MAP = new ConcurrentHashMap<>();

  /**
   * Make a new {@link PreferencesService}, or use the previous one if it is in the map.
   *
   * @param folderName preferences service folder name
   * @return service
   */
  public PreferencesService create(final String folderName) {
    if (PREFERENCES_MAP.containsKey(folderName)) {
      return PREFERENCES_MAP.get(folderName);
    } else {
      final PreferencesService service = new PreferencesService(folderName);
      PREFERENCES_MAP.put(folderName, service);
      return service;
    }
  }

  /**
   * Save all {@link PreferencesService} that are loaded into the map.
   */
  public void saveAllCached() {
    PREFERENCES_MAP.values().forEach(PreferencesService::save);
  }

  public Collection<PreferencesService> getAllPreferencesServices() {
    return PREFERENCES_MAP.values();
  }

}
