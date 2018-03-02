package com.neuronrobotics.bowlerbuilder.model.preferences;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads/saves preferences from/to a file.
 */
public class PreferencesService {

  private static final Logger LOGGER
      = LoggerUtilities.getLogger(PreferencesService.class.getSimpleName());
  private final String prefsSaveDirPath;
  private final String prefsSaveFilePath;
  private final Map<String, List<PreferenceListener>> listeners;
  private Map<String, Serializable> data;

  /**
   * The folder name is used to contain preferences within its own folder on disk, separate from
   * other preferences services.
   *
   * @param folderName unique folder name
   */
  public PreferencesService(final String folderName) {
    data = new ConcurrentHashMap<>();
    listeners = new ConcurrentHashMap<>();

    prefsSaveDirPath = LoggerUtilities.getBowlerDirectory()
        + File.separator
        + "preferences"
        + File.separator
        + folderName;

    prefsSaveFilePath = prefsSaveDirPath
        + File.separator
        + "preferences.ser";
  }

  /**
   * Get a value from the preferences data map.
   *
   * @param name name of entry
   * @param defaultValue default value if entry is not present
   * @param <T> type of entry
   * @return entry if present, else default value
   */
  public <T extends Serializable> T get(final String name, final T defaultValue) {
    if (data.containsKey(name)) {
      final Serializable value = data.get(name);
      if (value.getClass().isInstance(defaultValue)) {
        return (T) value;
      } else {
        throw new RuntimeException(
            "Preferences map entry type is not aligned with default value type.");
      }
    } else {
      data.put(name, defaultValue);
      return defaultValue;
    }
  }

  /**
   * Attach a listener to the entry. The listener will be notified when a value is set to the
   * entry.
   *
   * @param name entry key
   * @param listener listener to notify
   * @param <T> type of entry
   */
  public <T extends Serializable> void addListener(final String name,
      final PreferenceListener<T> listener) {
    if (listeners.containsKey(name)) {
      listeners.get(name).add(listener);
    } else {
      final List<PreferenceListener> newList = new ArrayList<>();
      newList.add(listener);
      listeners.put(name, newList);
    }
  }

  /**
   * Set a value in the preferences data map.
   *
   * @param name name of entry
   * @param value value of entry
   * @param <T> type of entry
   */
  public <T extends Serializable> void set(final String name, final T value) {
    LOGGER.fine("Set " + name + " to " + value);
    final Serializable prev = data.put(name, value);
    if (listeners.containsKey(name)) {
      listeners.get(name).forEach(listener -> listener.changed(prev, value));
    }
  }

  /**
   * Load in preferences from the save file.
   */
  public void load() {
    LOGGER.fine("Trying to load preferences from: " + prefsSaveFilePath);
    final File saveFile = new File(prefsSaveFilePath);
    if (saveFile.exists() && !saveFile.isDirectory()) {
      try (ObjectInputStream stream
          = new ObjectInputStream(new FileInputStream(prefsSaveFilePath))) {
        data = (Map<String, Serializable>) stream.readObject();
      } catch (final IOException e) {
        LOGGER.log(Level.SEVERE,
            "Could not open preferences save file.\n" + Throwables.getStackTraceAsString(e));
      } catch (final ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE,
            "Could not load preferences.\n" + Throwables.getStackTraceAsString(e));
      }
    } else {
      LOGGER.log(Level.SEVERE,
          "Preferences save file does not exist or is a directory: " + prefsSaveFilePath);
    }
  }

  /**
   * Overwrite the preferences file with the current preferences.
   */
  public void save() {
    LOGGER.fine("Trying to save preferences to: " + prefsSaveDirPath);
    final File saveDirectory = new File(prefsSaveDirPath);
    if (saveDirectory.exists() || saveDirectory.mkdirs()) {
      try (ObjectOutputStream stream
          = new ObjectOutputStream(new FileOutputStream(prefsSaveFilePath))) {
        stream.writeObject(data);
      } catch (final FileNotFoundException e) {
        LOGGER.log(Level.SEVERE,
            "Could not find preferences save file.\n" + Throwables.getStackTraceAsString(e));
      } catch (final IOException e) {
        LOGGER.log(Level.SEVERE,
            "Could not load preferences.\n" + Throwables.getStackTraceAsString(e));
      }
    } else {
      LOGGER.log(Level.SEVERE,
          "Could not create file to save preferences for save file: " + prefsSaveFilePath);
    }
  }

  public Collection<Serializable> getAllValues() {
    return data.values();
  }

  public Map<String, Serializable> getAll() {
    return data;
  }

}
