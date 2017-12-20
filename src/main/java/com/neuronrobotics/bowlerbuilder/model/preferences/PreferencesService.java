package com.neuronrobotics.bowlerbuilder.model.preferences;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;

/**
 * Loads and saves {@link Preferences} to a file.
 */
public class PreferencesService {

  private static final String prefsSaveDirPath = LoggerUtilities.getBowlerDirectory()
      + File.separator
      + "preferences";
  private static final String prefsSaveFilePath = prefsSaveDirPath
      + File.separator
      + "preferences.ser";

  /**
   * Load saved preferences in from the default save file.
   *
   * @return preferences from file
   */
  public Optional<Preferences> loadPreferencesFromFile() {
    Optional<Preferences> preferences = Optional.empty();

    try {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(
          IOUtils.toString(Files.readAllBytes(Paths.get(prefsSaveFilePath)), UTF_8));

      Map<String, Property> data = new HashMap<>();

      IntegerProperty fontSize = new SimpleIntegerProperty(null, "Font Size");
      fontSize.setValue(jsonObject.getInt("Font Size"));
      data.put("Font Size", fontSize);

      preferences = Optional.of(new Preferences(data));
    } catch (FileNotFoundException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not open FileInputStream when loading preferences.\n"
              + Throwables.getStackTraceAsString(e));
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not open ObjectInputStream when loading preferences.\n"
              + Throwables.getStackTraceAsString(e));
    }

    return preferences;
  }

  /**
   * Save a Preferences to the default save file.
   *
   * @param preferences preferences to save
   */
  public void savePreferencesToFile(Preferences preferences) {
    try {
      File preferencesSaveDirectory = new File(prefsSaveDirPath);

      if (preferencesSaveDirectory.exists() || preferencesSaveDirectory.mkdirs()) {
        JSONObject jsonObject = new JSONObject();
        preferences.getPropertyMap().forEach((key, value) -> jsonObject.put(key, value.getValue()));

        Files.write(
            Paths.get(prefsSaveFilePath),
            jsonObject.toString().getBytes(Charset.forName("UTF-8")));
      } else {
        LoggerUtilities.getLogger().log(Level.SEVERE,
            "Creating preferences directory failed.\n");
      }
    } catch (FileNotFoundException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not create FileOutputStream when saving.\n"
              + Throwables.getStackTraceAsString(e));
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not create ObjectOutputStream when saving\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Get the default Preferences.
   *
   * @return default preferences
   */
  public Preferences getDefaultPreferences() {
    Map<String, Property> data = new HashMap<>();

    IntegerProperty fontSize = new SimpleIntegerProperty(null, "Font Size");
    fontSize.setValue(14);
    data.put("Font Size", fontSize);

    return new Preferences(data);
  }

}
