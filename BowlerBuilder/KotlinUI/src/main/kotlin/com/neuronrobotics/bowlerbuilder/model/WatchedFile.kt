/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model

import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchEvent
import java.nio.file.WatchService
import kotlin.concurrent.thread

enum class WatchedFileChange {
    MODIFIED, DELETED, NOTHING
}

/**
 * A file paired with a [WatchService].
 *
 * @param file The file to watch.
 */
internal class WatchedFile
internal constructor(
    val file: File
) {

    private var lastEvent: WatchEvent<Path>? = null

    init {
        require(file.isFile) {
            "The supplied file must be a normal file."
        }

        require(file.parentFile.isDirectory) {
            "The supplied file's parent file must be a directory."
        }

        thread(isDaemon = true) {
            file.toPath().fileSystem.newWatchService().use { watchService ->
                /**
                 * Need to register both [ENTRY_CREATE] and [ENTRY_MODIFY]. Some editors
                 * overwrite the whole file (like gedit) and other modify the file on disk (like
                 * vim).
                 */
                file.parentFile.toPath().register(
                    watchService,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY
                )

                while (true) {
                    val wk = watchService.take()

                    wk.pollEvents().forEach {
                        @Suppress("UNCHECKED_CAST")
                        if (it.kind() != OVERFLOW) {
                            // Javadocs says this cast is fine
                            it as WatchEvent<Path>

                            if (it.context().endsWith(file.name)) {
                                lastEvent = it
                            }
                        }
                    }

                    val valid = wk.reset()
                    if (!valid) {
                        throw IllegalStateException(
                            """
                            |Watched directory became invalid.
                            |File path: ${file.absolutePath}
                            """.trimMargin()
                        )
                    }
                }
            }
        }
    }

    /**
     * Returns the most recent file change event.
     *
     * @return The most recent file change event.
     */
    fun wasFileChangedSinceLastCheck(): WatchedFileChange =
        when (lastEvent?.kind()) {
            ENTRY_CREATE, ENTRY_MODIFY -> {
                lastEvent = null
                WatchedFileChange.MODIFIED
            }
            ENTRY_DELETE -> {
                lastEvent = null
                WatchedFileChange.DELETED
            }
            null -> WatchedFileChange.NOTHING
            else -> throw IllegalStateException("Unknown event: $lastEvent")
        }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(WatchedFile::class.java.simpleName)
    }
}
