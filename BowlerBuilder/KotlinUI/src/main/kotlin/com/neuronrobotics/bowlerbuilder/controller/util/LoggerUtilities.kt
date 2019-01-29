/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.controller.util

import com.neuronrobotics.bowlerkernel.settings.BOWLER_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.LOGS_DIRECTORY
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

internal class LoggerUtilities private constructor() {

    init {
        throw UnsupportedOperationException("This is a utility class!")
    }

    // We can'translate call a logger here instead because we are the logger!
    @SuppressWarnings("PrintStackTrace")
    companion object {

        const val BOWLERBUILDER_DIRECTORY = "BowlerBuilder"

        // Log file parent directory path
        private val logFileDirPath: String = Paths.get(
            System.getProperty("user.home"),
            BOWLER_DIRECTORY,
            BOWLERBUILDER_DIRECTORY,
            LOGS_DIRECTORY
        ).toAbsolutePath().toString()

        private val logFileName =
            SimpleDateFormat("yyyyMMddHHmmss'.txt'", Locale("en", "US")).format(Date())

        // Log file path
        private val logFilePath: String = Paths.get(
            logFileDirPath,
            logFileName
        ).toAbsolutePath().toString()

        // FileHandler that saves to the log file
        private var fileHandler: FileHandler? = null

        // Previous logger names
        private val loggerNames = mutableSetOf<String>()

        init {
            val testFile = File(logFileDirPath)
            try {
                if (testFile.exists() || testFile.mkdirs()) {
                    fileHandler = FileHandler(logFilePath, true)
                    fileHandler!!.formatter = SimpleFormatter()
                } else {
                    throw IOException(
                        "LoggerUtilities could not create the logging file: $logFilePath"
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * Setup a logger with handlers and set its log level to [Level.ALL].
         *
         * @param name The logger name.
         * @return The new logger.
         */
        fun getLogger(name: String): Logger {
            if (!loggerNames.add(name)) {
                throw UnsupportedOperationException(
                    "Cannot add logger of name: $name. A logger with the same name already exists."
                )
            }

            return Logger.getLogger(name).apply {
                addHandler(ConsoleHandler())
                addHandler(fileHandler!!)
                level = Level.ALL
            }
        }

        /**
         * Returns the content of the current session's log file.
         *
         * @return The log file contents.
         */
        internal fun readCurrentLogFile() = File(logFilePath).readText()

        /**
         * Returns a file input stream for the current session's log file.
         *
         * @return A log file stream.
         */
        internal fun currentLogFileStream() = File(logFilePath).inputStream()

        /**
         * Returns the current session's log file name.
         *
         * @return The log file name.
         */
        internal fun currentLogFileName() = logFileName

        /**
         * Returns the current application version string.
         *
         * @return The application version.
         */
        fun getApplicationVersion(): String {
            val prop = Properties().apply {
                load(
                    File(
                        LoggerUtilities::class.java.classLoader
                            .getResource("version.properties").toURI()
                    ).reader()
                )
            }

            return prop["version"] as String
        }
    }
}
