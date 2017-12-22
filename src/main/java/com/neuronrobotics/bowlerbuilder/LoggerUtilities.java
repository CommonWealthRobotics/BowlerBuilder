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
import org.apache.commons.io.FileUtils;

public final class LoggerUtilities {

  //Log file parent directory path
  private static String logFileDirPath;

  //Log file path
  private static String logFilePath;

  //FileHandler that saves to the log file
  private static FileHandler fileHandler;

  //Previous logger names
  private static final Collection<String> loggerNames;

  static {
    logFileDirPath = getBowlerDirectory()
        + File.separator
        + "logs"
        + File.separator;

    logFilePath = logFileDirPath
        + new SimpleDateFormat("yyyyMMddHHmmss'.txt'", new Locale("en", "US"))
        .format(new Date());

    File testFile = new File(logFileDirPath);
    try {
      if (testFile.exists() || testFile.mkdirs()) {
        fileHandler = new FileHandler(logFilePath, true);
        fileHandler.setFormatter(new SimpleFormatter());
      } else {
        throw new IOException("LoggerUtilities could not create the logging file: " + logFilePath);
      }
    } catch (IOException e) {
      //We can't call a logger here instead because we are the logger!
      e.printStackTrace(); //NOPMD
    }

    loggerNames = new ArrayList<>();
  }

  private LoggerUtilities() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  public static String getLogFileDirPath() {
    return logFileDirPath;
  }

  public static String getLogFilePath() {
    return logFilePath;
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
  public static Thread newLoggingThread(Logger logger, Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.setUncaughtExceptionHandler((t, e) ->
        logger.log(Level.SEVERE, Throwables.getStackTraceAsString(e)));
    return thread;
  }

  /**
   * Setup a logger with handlers and set its log level to ALL.
   *
   * @param name logger name
   * @return new logger
   */
  public static Logger getLogger(String name) {
    if (loggerNames.contains(name)) {
      throw new UnsupportedOperationException(
          "Cannot add logger of name: " + name + ". A logger with the same name already exists.");
    }

    Logger logger = Logger.getLogger(name);
    logger.addHandler(new ConsoleHandler());
    logger.addHandler(LoggerUtilities.getFileHandler());
    logger.setLevel(Level.ALL);
    return logger;
  }

}
