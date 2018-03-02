package com.neuronrobotics.bowlerbuilder;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.annotation.Nonnull;
import org.apache.commons.io.FileUtils;

public final class LoggerUtilities {

  //Log file parent directory path
  private static final String LOG_FILE_DIR_PATH;

  //Log file path
  private static final String LOG_FILE_PATH;

  //FileHandler that saves to the log file
  private static FileHandler fileHandler;

  //Previous logger names
  private static final Collection<String> LOGGER_NAMES;

  static {
    LOG_FILE_DIR_PATH = getBowlerDirectory()
        + File.separator
        + "logs"
        + File.separator;

    LOG_FILE_PATH = LOG_FILE_DIR_PATH
        + new SimpleDateFormat("yyyyMMddHHmmss'.txt'", new Locale("en", "US"))
        .format(new Date());

    final File testFile = new File(LOG_FILE_DIR_PATH);
    try {
      if (testFile.exists() || testFile.mkdirs()) {
        fileHandler = new FileHandler(LOG_FILE_PATH, true);
        fileHandler.setFormatter(new SimpleFormatter());
      } else {
        throw new IOException(
            "LoggerUtilities could not create the logging file: " + LOG_FILE_PATH);
      }
    } catch (final IOException e) {
      //We can'translate call a logger here instead because we are the logger!
      e.printStackTrace(); //NOPMD
    }

    LOGGER_NAMES = new ArrayList<>();
  }

  private LoggerUtilities() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  public static String getLogFileDirPath() {
    return LOG_FILE_DIR_PATH;
  }

  public static String getLogFilePath() {
    return LOG_FILE_PATH;
  }

  public static FileHandler getFileHandler() {
    return fileHandler;
  }

  /**
   * Get the BowlerBuilder directory for saving, logging, etc.
   *
   * @return path to BowlerBuilder directory
   */
  public static String getBowlerDirectory() {
    return FileUtils.getUserDirectoryPath()
        + File.separator
        + "BowlerBuilder";
  }

  /**
   * Return a new thread that logs uncaught exceptions.
   *
   * @param runnable thread runnable
   * @return logging thread
   */
  public static Thread newLoggingThread(@Nonnull final Logger logger,
      @Nonnull final Runnable runnable) {
    final Thread thread = new Thread(runnable);
    thread.setUncaughtExceptionHandler((exceptionThread, exception) ->
        logger.log(Level.SEVERE, "Uncaught exception in thread: " + exceptionThread.getName()
            + "\n" + Throwables.getStackTraceAsString(exception)));
    return thread;
  }

  /**
   * Setup a logger with handlers and set its log level to ALL.
   *
   * @param name logger name
   * @return new logger
   */
  public static Logger getLogger(@Nonnull final String name) {
    if (LOGGER_NAMES.contains(name)) {
      throw new UnsupportedOperationException(
          "Cannot add logger of name: " + name + ". A logger with the same name already exists.");
    }

    final Logger logger = Logger.getLogger(name);
    logger.addHandler(new ConsoleHandler());
    logger.addHandler(LoggerUtilities.getFileHandler());
    logger.setLevel(Level.ALL);
    return logger;
  }

}
