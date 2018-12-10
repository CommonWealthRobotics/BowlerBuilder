/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.webbrowser

import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.Gist
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.web.WebEngine
import tornadofx.*

class WebBrowserController : Controller() {

    val itemsOnPageProperty: ObservableList<WebBrowserScript> =
        FXCollections.observableArrayList<WebBrowserScript>()
    private val scriptRunner: ScriptRunner by di()
    private val scriptEditorFactory: ScriptEditorFactory by di()

    fun loadItemsOnPage(currentUrl: String, engine: WebEngine) {
        if (currentUrl.split("//").size < 2) {
            // Don't call ScriptingEngine.getCurrentGist() with less than two elements because it
            // throws an exception
            itemsOnPageProperty.clear()
        } else {
            ScriptingEngine.getCurrentGist(currentUrl, engine)
                .map {
                    WebBrowserScript(
                        currentUrl,
                        GistFile(
                            Gist(getGitUrlFromPageUrl(currentUrl, it), ""),
                            ""
                        )
                    )
                }.flatMap { script ->
                    val files = try {
                        ScriptingEngine.filesInGit(script.gistFile.gist.gitUrl)
                    } catch (ex: Exception) {
                        // This is the ScriptingEngine login manager being unhappy
                        emptyList<String>()
                    }

                    files.map {
                        script.copy(gistFile = script.gistFile.copy(filename = it))
                    }
                }.filter {
                    it.gistFile.filename != "csgDatabase.json"
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

    fun runScript(currentScript: WebBrowserScript) {
        if (currentScript == WebBrowserScript.empty) {
            return
        }

        scriptRunner.runScript(currentScript.gistFile.gist.gitUrl, currentScript.gistFile.filename)
        editScript(currentScript)
    }

    fun editScript(currentScript: WebBrowserScript) {
        if (currentScript == WebBrowserScript.empty) {
            return
        }

        scriptEditorFactory
            .createAndOpenScriptEditor(currentScript.gistFile)
            .apply {
                runLater { gotoLine(0) }
            }
    }

    fun doesUserOwnScript(currentScript: WebBrowserScript): Boolean {
        if (currentScript == WebBrowserScript.empty) {
            return false
        }

        val currentFile = ScriptingEngine.fileFromGit(
            currentScript.gistFile.gist.gitUrl,
            currentScript.gistFile.filename
        )

        // TODO: checkOwner() doesn't work
        return ScriptingEngine.checkOwner(currentFile)
    }

    fun cloneScript(currentScript: WebBrowserScript): WebBrowserScript {
        if (currentScript == WebBrowserScript.empty) {
            return WebBrowserScript.empty
        }

        val gist = ScriptingEngine.fork(
            ScriptingEngine.urlToGist(currentScript.gistFile.gist.gitUrl)
        ) ?: throw IllegalStateException("Failed to fork script.")

        return currentScript.copy(
            pageUrl = gist.htmlUrl,
            gistFile = currentScript.gistFile.copy(
                gist = currentScript.gistFile.gist.copy(
                    gitUrl = gist.gitPushUrl
                )
            )
        )
    }

    /**
     * Maps the URL for a page containing a Git resource to the URL for that resource.
     *
     * @param pageUrl The page URL.
     * @param gist The gist on the page, empty if no gist.
     * @return A Git URL.
     */
    private fun getGitUrlFromPageUrl(pageUrl: String, gist: String): String {
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
            "https://gist.github.com/$gist.git"
        }
    }
}
