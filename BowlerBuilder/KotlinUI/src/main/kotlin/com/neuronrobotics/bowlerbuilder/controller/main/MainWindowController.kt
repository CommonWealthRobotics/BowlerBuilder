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
package com.neuronrobotics.bowlerbuilder.controller.main

import arrow.core.Try
import arrow.core.recoverWith
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
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitHubFS
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultGistScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultTextScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.GistScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.TextScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import javafx.application.Platform
import org.greenrobot.eventbus.EventBus
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import org.kohsuke.github.GHGist
import org.kohsuke.github.GHGistFile
import org.kohsuke.github.GitHub
import tornadofx.*
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

class MainWindowController
@Inject constructor(
    private val cadScriptEditorFactory: CadScriptEditorFactory
) : Controller() {

    var credentials: Pair<String, String> = "" to ""
    var gitHub: Try<GitHub> = Try.raise(IllegalStateException("Not logged in."))
    var gitFS: Try<GitFS> = Try.raise(IllegalStateException("Not logged in."))

    /**
     * Opens a Gist file in an editor.
     */
    fun openGistFile(gist: GHGist, gistFile: GHGistFile) {
        GitHubFS.mapGistFileToFileOnDisk(
            gist, gistFile
        ).recoverWith {
            gitFS.flatMap { gitFS ->
                gitFS.cloneRepoAndGetFiles(
                    gist.gitPullUrl
                ).flatMap { files ->
                    Try {
                        files.first { it.name == gistFile.fileName }
                    }
                }
            }
        }.map {
            cadScriptEditorFactory.createAndOpenScriptEditor(gist.gitPullUrl, it)
        }
    }

    /**
     * Clear the local git cache.
     */
    fun deleteGitCache() {
        gitFS.toEither {
            LOGGER.warning {
                """
                |Cannot delete Git cache:
                |${Throwables.getStackTraceAsString(it)}
                """.trimMargin()
            }
        }.map {
            it.deleteCache()
            beginForceQuit()
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

        val mainUIEventBus = EventBus.builder()
            .sendNoSubscriberEvent(false)
            .logger(BowlerEventBusLogger("MainUIEventBus"))
            .build()

        /**
         * Try to close gracefully and start a scheduled task to forcibly close the application.
         * Publishes a [ApplicationClosingEvent] on the [MainWindowController.mainUIEventBus].
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

            mainUIEventBus.post(ApplicationClosingEvent)
            Platform.exit()
        }
    }
}
