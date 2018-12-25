/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import arrow.core.Try
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.Gist
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.model.Organization
import com.neuronrobotics.bowlerbuilder.model.Repository
import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.main.event.ApplicationClosingEvent
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.common.DeviceManager
import javafx.application.Platform
import org.apache.commons.io.FileUtils
import org.kohsuke.github.GitHub
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

class MainWindowController
@Inject constructor(
    private val cadScriptEditorFactory: CadScriptEditorFactory
) : Controller() {

    var credentials: Pair<String, String> = "" to ""
    var gitHub: Try<GitHub> = Try.raise(IllegalStateException("Not logged in."))

    /**
     * Load the authenticated user's gists.
     */
    fun loadUserGists(): ImmutableList<Gist> {
        return gitHub.fold(
            { emptyImmutableList() },
            {
                it.myself
                    .listGists()
                    .map {
                        Gist(
                            gitUrl = it.gitPushUrl,
                            id = it.id.toLong(),
                            description = it.description
                        )
                    }.toImmutableList()
            }
        )
    }

    /**
     * Load the authenticated user's organizations.
     */
    fun loadUserOrgs(): ImmutableList<Organization> {
        return gitHub.fold(
            { emptyImmutableList() },
            {
                it.myOrganizations
                    .map {
                        Organization(
                            gitUrl = it.value.htmlUrl,
                            name = it.key,
                            repositories = it.value.repositories
                                .map {
                                    Repository(
                                        gitUrl = it.value.gitTransportUrl,
                                        name = it.key
                                    )
                                }.toImmutableList()
                        )
                    }.toImmutableList()
            }
        )
    }

    /**
     * Load the files in the [gist].
     */
    fun loadFilesInGist(gist: Gist): ImmutableList<GistFile> {
        return gitHub.fold(
            { emptyImmutableList() },
            {
                it.getGist(gist.id.toString()).let { gist ->
                    gist.files.values.map {
                        GistFile(
                            Gist(gist.htmlUrl, gist.id.toLong(), gist.description),
                            it.fileName
                        )
                    }.toImmutableList()
                }
            }
        )
    }

    /**
     * Open the [file] in an editor.
     */
    fun openGistFile(file: GistFile) {
        cadScriptEditorFactory.createAndOpenScriptEditor(file)
    }

    /**
     * Clear the local git cache.
     */
    fun deleteLocalCache() {
        try {
            FileUtils.deleteDirectory(
                File(ScriptingEngine.getWorkspace().absolutePath + "/gistcache/")
            )

            beginForceQuit()
        } catch (e: IOException) {
            LOGGER.severe {
                """
                |Unable to delete cache.
                |${Throwables.getStackTraceAsString(e)}
                """.trimMargin()
            }
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(MainWindowController::class.java.simpleName)
        const val BOWLERBUILDER_DIRECTORY = "BowlerBuilder"

        /**
         * Try to close gracefully and start a scheduled task to forcibly close the application.
         * Publishes a [ApplicationClosingEvent] on the [MainWindowView.mainUIEventBus].
         */
        fun beginForceQuit() {
            // Need to make sure the VM exits; sometimes a rouge thread is running
            // Wait 10 seconds before killing the VM
            val timer = Timer(true)
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        LOGGER.severe {
                            "Still alive for some reason. Printing threads and killing VM..."
                        }

                        LOGGER.fine {
                            Thread.getAllStackTraces().entries.joinToString("\n\n") {
                                """
                            |Thread: ${it.key}
                            |Stacktrace:
                            |${it.value.joinToString(
                                    separator = "\n\t",
                                    prefix = "\t"
                                )}
                            """.trimMargin()
                            }
                        }

                        Runtime.getRuntime().exit(1) // Abnormal exit
                    }
                },
                10000
            )

            MainWindowView.mainUIEventBus.post(ApplicationClosingEvent)
            disconnectAllDevices()
            Platform.exit()
        }

        private fun disconnectAllDevices() {
            DeviceManager.listConnectedDevice().forEach {
                val device = DeviceManager.getSpecificDevice(it)

                if (device.isAvailable) {
                    device.disconnect()
                }

                DeviceManager.remove(device)
            }
        }
    }
}
