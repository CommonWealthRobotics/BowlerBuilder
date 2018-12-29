/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.webbrowser

import arrow.core.Try
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.recoverWith
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptResultHandler
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.util.cloneRepoAndGetFiles
import com.neuronrobotics.bowlerbuilder.controller.util.forkGist
import com.neuronrobotics.bowlerbuilder.model.Gist
import com.neuronrobotics.bowlerbuilder.model.GistFileOnDisk
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerkernel.scripting.DefaultGistScriptFactory
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.toImmutableList
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.web.WebEngine
import org.jsoup.Jsoup
import tornadofx.Controller
import tornadofx.runLater
import java.io.File
import java.io.StringWriter
import javax.inject.Inject
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class WebBrowserController
@Inject constructor(
    private val scriptFactory: DefaultGistScriptFactory.Factory,
    private val cadScriptEditorFactory: CadScriptEditorFactory,
    private val scriptResultHandler: ScriptResultHandler
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
                val files = cloneRepoAndGetFiles(
                    getInstanceOf<MainWindowController>().credentials,
                    url
                ).fold(
                    { emptyImmutableList<File>() },
                    { it }
                )

                files.map {
                    WebBrowserScript(
                        currentUrl,
                        GistFileOnDisk(
                            Gist(url, gistId, ""),
                            it
                        )
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
     * Runs the [currentScript] with the injected [scriptFactory].
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

        getInstanceOf<MainWindowController>().gitHub.map {
            scriptFactory.create(
                it
            ).createScriptFromGist(
                currentScript.gistFile.gist.id,
                currentScript.gistFile.file.name
            ).flatMap {
                it.runScript(emptyImmutableList())
            }.bimap(
                {
                    LOGGER.warning {
                        """
                        |Error while running script:
                        |$it
                        """.trimMargin()
                    }
                    it
                },
                {
                    scriptResultHandler.handleResult(it)
                }
            )
        }.recoverWith {
            LOGGER.warning {
                """
                |Error while retrieving GitHub:
                |$it
                """.trimMargin()
            }
            Try.raise(it)
        }
    }

    /**
     * Returns whether the currently logged in user owns the [currentScript].
     */
    fun doesUserOwnScript(currentScript: WebBrowserScript): Try<Boolean> {
        return getInstanceOf<MainWindowController>().gitHub.flatMap { gitHub ->
            Try {
                gitHub.myself.listGists().firstOrNull {
                    it.gitPullUrl == currentScript.gistFile.gist.gitUrl
                } != null
            }.recoverWith {
                Try {
                    gitHub.myself.listRepositories().first {
                        it.gitTransportUrl == currentScript.gistFile.gist.gitUrl
                    }.hasPushAccess()
                }
            }
        }
    }

    /**
     * Maps a [gistUrl] to its id.
     *
     * @param gistUrl The gist URL, i.e.
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     */
    private fun gistUrlToId(gistUrl: String): String =
        gistUrl
            .removePrefix("http://gist.github.com/")
            .removePrefix("https://gist.github.com/")
            .removeSuffix(".git/")
            .removeSuffix(".git")

    /**
     * Clones the [currentScript] and opens it in an editor.
     */
    fun forkScript(currentScript: WebBrowserScript): Try<WebBrowserScript> {
        if (currentScript == WebBrowserScript.empty) {
            return Try.just(WebBrowserScript.empty)
        }

        LOGGER.fine(
            """
            |Forking gist:
            |$currentScript
            """.trimMargin()
        )

        val gist = getInstanceOf<MainWindowController>().gitHub.flatMap {
            forkGist(
                it,
                currentScript.gistFile.gist.id
            )
        }.recoverWith {
            LOGGER.severe(
                """
                |Failed to fork gist:
                |${Throwables.getStackTraceAsString(it)}
                """.trimMargin()
            )
            Try.raise(it)
        }

        return gist.map {
            val scriptClone = currentScript.copy(
                pageUrl = it.htmlUrl.toString(),
                gistFile = currentScript.gistFile.copy(
                    gist = currentScript.gistFile.gist.copy(
                        gitUrl = it.gitPullUrl
                    )
                )
            )

            LOGGER.fine(
                """
                |Forked to:
                |$scriptClone
                """.trimMargin()
            )

            scriptClone
        }
    }

    /**
     * Opens the [currentScript] in an editor.
     */
    fun editScript(currentScript: WebBrowserScript) {
        if (currentScript == WebBrowserScript.empty) {
            return
        }

        LOGGER.fine(
            """
            |Editing script:
            |$currentScript
            """.trimMargin()
        )

        cadScriptEditorFactory
            .createAndOpenScriptEditor(
                currentScript.gistFile.gist.gitUrl, // TODO: Wrong URL
                currentScript.gistFile.file
            ).apply {
                runLater { editor.gotoLine(0) }
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
