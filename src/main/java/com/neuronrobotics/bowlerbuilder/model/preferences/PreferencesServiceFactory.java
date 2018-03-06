/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.model.preferences;

import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PreferencesServiceFactory {

  private static final Map<String, PreferencesService> preferencesMap = new ConcurrentHashMap<>();

  /**
   * Make a new {@link PreferencesService}, or use the previous one if it is in the map.
   *
   * @param folderName preferences service folder name
   * @return service
   */
  public PreferencesService create(String folderName) {
    if (preferencesMap.containsKey(folderName)) {
      return preferencesMap.get(folderName);
    } else {
      PreferencesService service = new PreferencesService(folderName);
      preferencesMap.put(folderName, service);
      return service;
    }
  }

  /**
   * Save all {@link PreferencesService} that are loaded into the map.
   */
  public void saveAllCached() {
    preferencesMap.values().forEach(PreferencesService::save);
  }

  public Collection<PreferencesService> getAllPreferencesServices() {
    return preferencesMap.values();
  }

}
