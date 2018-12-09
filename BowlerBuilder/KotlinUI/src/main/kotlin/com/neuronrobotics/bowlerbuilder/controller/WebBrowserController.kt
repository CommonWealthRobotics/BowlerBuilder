package com.neuronrobotics.bowlerbuilder.controller

import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.web.WebEngine
import tornadofx.*

class WebBrowserController : Controller() {

    val itemsOnPageProperty: ObservableList<WebBrowserScript> =
        FXCollections.observableArrayList<WebBrowserScript>()

    fun loadItemsOnPage(currentUrl: String, engine: WebEngine) {
        if (currentUrl.split("//").size < 2) {
            // Don't call ScriptingEngine.getCurrentGist() with less than two elements because it
            // throws an exception
            itemsOnPageProperty.clear()
        } else {
            ScriptingEngine.getCurrentGist(currentUrl, engine)
                .map {
                    WebBrowserScript(currentUrl, getGitUrlFromPageUrl(currentUrl, it), "")
                }.flatMap { script ->
                    ScriptingEngine.filesInGit(script.gitUrl)
                        .map {
                            script.copy(filename = it)
                        }
                }.filter {
                    it.filename != "csgDatabase.json"
                }.let {
                    itemsOnPageProperty.setAll(it)
                }
        }
    }

    fun runScript(currentScript: WebBrowserScript) {
        TODO("not implemented")
    }

    fun editScript(currentScript: WebBrowserScript) {
        TODO("not implemented")
    }

    fun doesUserOwnScript(currentScript: WebBrowserScript): Boolean {
        if (currentScript == WebBrowserScript.empty) {
            return false
        }

        val currentFile = ScriptingEngine.fileFromGit(currentScript.gitUrl, currentScript.filename)
        // TODO: checkOwner() doesn't work
        return ScriptingEngine.checkOwner(currentFile)
    }

    fun cloneScript(currentScript: WebBrowserScript): WebBrowserScript {
        if (currentScript == WebBrowserScript.empty) {
            return WebBrowserScript.empty
        }

        val gist = ScriptingEngine.fork(ScriptingEngine.urlToGist(currentScript.gitUrl))
            ?: throw IllegalStateException("Failed to fork script.")
        return currentScript.copy(pageUrl = gist.htmlUrl, gitUrl = gist.gitPushUrl)
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
