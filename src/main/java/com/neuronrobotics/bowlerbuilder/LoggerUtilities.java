package com.neuronrobotics.bowlerbuilder;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.FileUtils;

public final class LoggerUtilities {

  private static final Logger logger = Logger.getLogger("");

  static {
    String path = getBowlerDirectory()
        + File.separator
        + "logs"
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

  private LoggerUtilities() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  public static Logger getLogger() {
    return logger;
  }

  /**
   * Get the BowlerBuilder directory for saving, logging, etc.
   *
   * @return Path to BowlerBuilder directory
   */
  public static String getBowlerDirectory() {
    return FileUtils.getUserDirectoryPath()
        + File.separator
        + "BowlerBuilder";
  }

  /**
   * Return a new thread that logs uncaught exceptions.
   *
   * @param runnable Thread runnable
   * @return logging thread
   */
  public static Thread newLoggingThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.setUncaughtExceptionHandler((t, e) ->
        LoggerUtilities.getLogger().log(Level.SEVERE, Throwables.getStackTraceAsString(e)));
    return thread;
  }

}
