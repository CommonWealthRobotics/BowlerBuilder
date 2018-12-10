/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.Gist
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.model.Organization
import com.neuronrobotics.bowlerbuilder.model.Repository
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.util.toImmutableList
import org.apache.commons.io.FileUtils
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import java.util.logging.Level

class MainWindowController : Controller() {

    private val scriptEditorFactory: ScriptEditorFactory by di()

    fun loadUserGists(): ImmutableList<Gist> {
        return ScriptingEngine.getGithub()
            .myself
            .listGists()
            .map {
                Gist(
                    gitUrl = it.gitPushUrl,
                    description = it.description
                )
            }.toImmutableList()
    }

    fun loadFilesInGist(gist: Gist): ImmutableList<GistFile> {
        return ScriptingEngine.filesInGit(gist.gitUrl)
            .map {
                GistFile(gist, it)
            }.toImmutableList()
    }

    fun openGistFile(file: GistFile) {
        scriptEditorFactory.createAndOpenScriptEditor(file)
    }

    fun loadUserOrgs(): ImmutableList<Organization> {
        return ScriptingEngine.getGithub()
            .myOrganizations
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

                    val threads = Thread.getAllStackTraces().keys
                    val threadString = StringBuilder()
                    threads.forEach { item -> threadString.append(item).append("\n") }
                    LOGGER.log(Level.FINE, threadString.toString())

                    Runtime.getRuntime().exit(1) // Abnormal exit
                }
            },
            10000
        )
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(MainWindowController::class.java.simpleName)
    }
}
