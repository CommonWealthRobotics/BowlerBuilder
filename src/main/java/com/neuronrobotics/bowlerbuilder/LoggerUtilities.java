package com.neuronrobotics.bowlerbuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LoggerUtilities {

  private static final Logger logger = Logger.getLogger("LOGGER_UTILITIES");

  public static Logger getLogger() {
    return logger;
  }

  private LoggerUtilities() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  static {
    String path = FileUtils.getUserDirectoryPath()
        + File.separator
        + "BowlerBuilder"
        + File.separator;
    File testFile = new File(path);
    try {
      if (testFile.exists() || testFile.mkdirs()) {
        FileHandler fileHandler = new FileHandler(
            path + new SimpleDateFormat("yyyyMMddHHmmss'.txt'", new Locale("en", "US"))
                .format(new Date()),
            true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
      } else {
        throw new IOException("LoggerUtilities could not create the logging file: " + path);
      }
    } catch (IOException e) {
      //We can't call a logger here instead because we are the logger!
      e.printStackTrace(); //NOPMD
    }
  }

}
