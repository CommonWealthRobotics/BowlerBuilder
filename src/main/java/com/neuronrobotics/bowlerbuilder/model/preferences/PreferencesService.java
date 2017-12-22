package com.neuronrobotics.bowlerbuilder.model.preferences;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  private static final Logger logger
      = LoggerUtilities.getLogger(PreferencesService.class.getSimpleName());

  private static final String prefsSaveDirPath = LoggerUtilities.getBowlerDirectory()
      + File.separator
      + "preferences";
  private static final String prefsSaveFilePath = prefsSaveDirPath
      + File.separator
      + "preferences.json";

  /**
   * Load saved preferences in from the default save file.
   *
   * @return preferences from file
   * @throws IOException reading bytes from the save file can fail
   */
  public Optional<Preferences> loadPreferencesFromFile() throws IOException {
    File saveFile = new File(prefsSaveFilePath);
    if (saveFile.exists() && !saveFile.isDirectory()) {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(
          IOUtils.toString(Files.readAllBytes(Paths.get(prefsSaveFilePath)), UTF_8));

      Map<String, Property> data = new HashMap<>();

      IntegerProperty fontSize = new SimpleIntegerProperty(null, "Font Size");
      fontSize.setValue(jsonObject.getInt("Font Size"));
      data.put("Font Size", fontSize);

      return Optional.of(new Preferences(data));
    }

    return Optional.empty();
  }

  /**
   * Save a Preferences to the default save file.
   *
   * @param preferences preferences to save
   * @throws IOException writing to the save file can fail
   */
  public void savePreferencesToFile(Preferences preferences) throws IOException {
    File preferencesSaveDirectory = new File(prefsSaveDirPath);

    if (preferencesSaveDirectory.exists() || preferencesSaveDirectory.mkdirs()) {
      JSONObject jsonObject = new JSONObject();
      preferences.getPropertyMap().forEach((key, value) -> jsonObject.put(key, value.getValue()));

      Files.write(
          Paths.get(prefsSaveFilePath),
          jsonObject.toString().getBytes(Charset.forName("UTF-8")));
    } else {
      logger.log(Level.SEVERE,
          "Creating preferences directory failed.\n");
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
