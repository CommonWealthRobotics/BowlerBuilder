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
package com.neuronrobotics.bowlerbuilder.controller.webbrowser

import arrow.core.Try
import arrow.core.getOrElse
import arrow.core.recoverWith
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.GitScriptRunner
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.model.Gist
import com.neuronrobotics.bowlerbuilder.model.GistFileOnDisk
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.web.WebEngine
import org.jsoup.Jsoup
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.toImmutableList
import tornadofx.*
import java.io.File
import java.io.StringWriter
import javax.inject.Inject
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class WebBrowserController
@Inject constructor(
    private val cadScriptEditorFactory: CadScriptEditorFactory,
    private val gitScriptRunner: GitScriptRunner
) : Controller() {

    val itemsOnPageProperty: ObservableList<WebBrowserScript> =
        FXCollections.observableArrayList<WebBrowserScript>()

    /**
     * Loads the scripts on the current page into [itemsOnPageProperty].
     *
     * @param currentUrl The url of the page.
     * @param engine The webview engine to pull DOM from.
     */
    @SuppressWarnings("TooGenericExceptionCaught")
    fun loadItemsOnPage(currentUrl: String, engine: WebEngine) {
        LOGGER.fine("Loading items for: $currentUrl")
        if (currentUrl.split("//").size < 2) {
            // Don't call getCurrentGistId() with less than two elements because it throws an
            // exception
            itemsOnPageProperty.clear()
        } else {
            getCurrentGistId(engine).flatMap { gistId ->
                val url = getGitUrlFromPageUrl(currentUrl, gistId)
                val files = getInstanceOf<MainWindowController>().gitFS.flatMap {
                    it.cloneRepoAndGetFiles(url)
                }.fold(
                    { emptyImmutableList<File>() },
                    { it }
                )

                files.map {
                    WebBrowserScript(
                        currentUrl,
                        GistFileOnDisk(Gist(url, gistId, ""), it)
                    )
                }
            }.filter {
                it.gistFile.file.name != "csgDatabase.json"
            }.let {
                itemsOnPageProperty.setAll(it)
            }
        }
    }

    /**
     * Formats the [url] so the JavaFX web engine can load it.
     */
    fun formatUrl(url: String) =
        if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }

    /**
     * Passes control through to [gitScriptRunner].
     *
     * @param currentScript The script to run.
     */
    fun runScript(currentScript: WebBrowserScript) {
        if (currentScript == WebBrowserScript.empty) {
            return
        }

        LOGGER.fine(
            """
            |Running script:
            |$currentScript
            """.trimMargin()
        )

        gitScriptRunner.runScript(
            currentScript.gistFile.gist.gitUrl,
            currentScript.gistFile.file.name
        )
    }

    /**
     * Passes control through to [gitScriptRunner].
     */
    fun stopScript() = gitScriptRunner.stopScript()

    /**
     * Opens the [currentScript] in an editor.
     */
    fun editScript(currentScript: WebBrowserScript) {
        if (currentScript == WebBrowserScript.empty) {
            return
        }

        val scriptFork = forkScript(currentScript)

        scriptFork.map {
            LOGGER.info(
                """
                |Editing script:
                |$it
                """.trimMargin()
            )

            cadScriptEditorFactory.createAndOpenScriptEditor(
                it.gistFile.gist.gitUrl,
                it.gistFile.file
            ).apply {
                runLater { editor.gotoLine(0) }
            }
        }
    }

    /**
     * Clones the [currentScript] and opens it in an editor.
     */
    private fun forkScript(currentScript: WebBrowserScript): Try<WebBrowserScript> {
        if (currentScript == WebBrowserScript.empty) {
            return Try.just(WebBrowserScript.empty)
        }

        LOGGER.fine(
            """
            |Forking gist:
            |$currentScript
            """.trimMargin()
        )

        return getInstanceOf<MainWindowController>().gitFS.flatMap {
            it.forkRepo(currentScript.gistFile.gist.gitUrl).map { gistForkUrl ->
                val scriptClone = currentScript.copy(
                    pageUrl = gistForkUrl,
                    gistFile = currentScript.gistFile.copy(
                        gist = currentScript.gistFile.gist.copy(
                            gitUrl = gistForkUrl
                        )
                    )
                )

                LOGGER.info(
                    """
                    |Forked to:
                    |$scriptClone
                    """.trimMargin()
                )

                scriptClone
            }
        }.recoverWith {
            LOGGER.severe(
                """
                |Failed to fork gist:
                |${Throwables.getStackTraceAsString(it)}
                """.trimMargin()
            )
            Try.raise(it)
        }
    }

    /**
     * Maps the URL for a page containing a Git resource to the URL for that resource.
     *
     * @param pageUrl The page URL.
     * @param gistId The gistId on the page, empty if no gistId.
     * @return A Git URL.
     */
    private fun getGitUrlFromPageUrl(pageUrl: String, gistId: String): String {
        return if (pageUrl.contains("https://github.com/")) {
            if (pageUrl.endsWith("/")) {
                if (pageUrl.endsWith(".git/")) {
                    pageUrl.substring(0, pageUrl.length - 1)
                } else {
                    pageUrl.substring(0, pageUrl.length - 1) + ".git"
                }
            } else if (pageUrl.endsWith(".git")) {
                pageUrl
            } else {
                "$pageUrl.git"
            }
        } else {
            "https://gist.github.com/$gistId.git"
        }
    }

    private fun getCurrentGistId(engine: WebEngine): ImmutableList<String> {
        val sw = StringWriter()

        TransformerFactory.newInstance().newTransformer().transform(
            DOMSource(engine.document),
            StreamResult(sw)
        )

        return returnFirstGistId(sw.buffer.toString())
    }

    private fun returnFirstGistId(html: String): ImmutableList<String> {
        return Try {
            Jsoup.parse(html)
                .select("script")
                .mapNotNull {
                    val srcElement = it.attributes()["src"]
                    if (srcElement.contains("https://gist.github.com/")) {
                        srcElement
                            .removePrefix("https://gist.github.com/")
                            .removeSuffix(".js")
                            .split("/")
                            .lastOrNull()
                    } else {
                        null
                    }
                }.toImmutableList()
        }.getOrElse {
            LOGGER.warning {
                """
                |Failure while parsing gist id's from document:
                |${Throwables.getStackTraceAsString(it)}
                """.trimMargin()
            }
            emptyImmutableList()
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(WebBrowserController::class.java.simpleName)
    }
}
