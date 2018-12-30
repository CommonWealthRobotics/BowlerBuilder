/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.main

import arrow.core.Try
import com.google.common.base.Throwables
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Scopes
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.neuronrobotics.bowlerbuilder.controller.gitmenu.LoginManager
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.AceCadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.main.event.ApplicationClosingEvent
import com.neuronrobotics.bowlerkernel.scripting.DefaultGistScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.DefaultScriptLanguageParser
import com.neuronrobotics.bowlerkernel.scripting.DefaultTextScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.GistScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguageParser
import com.neuronrobotics.bowlerkernel.scripting.TextScriptFactory
import com.neuronrobotics.bowlerkernel.util.BOWLERBUILDER_DIRECTORY
import com.neuronrobotics.bowlerkernel.util.GIT_CACHE_DIRECTORY
import javafx.application.Platform
import org.apache.commons.io.FileUtils
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import org.kohsuke.github.GitHub
import tornadofx.*
import java.io.File
import java.io.IOException
import java.nio.file.Paths
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
     * Open the [file] in an editor.
     */
    fun openGistFile(url: String, file: File) {
        cadScriptEditorFactory.createAndOpenScriptEditor(url, file)
    }

    /**
     * Clear the local git cache.
     */
    fun deleteLocalCache() {
        try {
            FileUtils.deleteDirectory(
                Paths.get(
                    System.getProperty("user.home"),
                    BOWLERBUILDER_DIRECTORY,
                    GIT_CACHE_DIRECTORY
                ).toFile()
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

        internal fun mainModule() = module {
            // KotlinUI dependencies
            bind<MainWindowView>().`in`(Scopes.SINGLETON)
            bind<MainWindowController>().`in`(Scopes.SINGLETON)
            bind<LoginManager>().`in`(Scopes.SINGLETON)
            bind<CadScriptEditorFactory>().to<AceCadScriptEditorFactory>()

            // Kernel dependencies
            bind<ScriptLanguageParser>().to<DefaultScriptLanguageParser>()
            bind<TextScriptFactory>().to<DefaultTextScriptFactory>()
            install(
                FactoryModuleBuilder()
                    .implement(
                        GistScriptFactory::class.java,
                        DefaultGistScriptFactory::class.java
                    )
                    .build(
                        DefaultGistScriptFactory.Factory::class.java
                    )
            )
        }

        val injector: Injector = Guice.createInjector(mainModule())

        inline fun <reified T> getInstanceOf(): T = injector.getInstance(key<T>())

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
            Platform.exit()
        }
    }
}
