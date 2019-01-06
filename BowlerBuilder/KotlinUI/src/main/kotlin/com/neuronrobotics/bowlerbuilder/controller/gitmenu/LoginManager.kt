/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import arrow.core.Try
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.util.cloneAssetRepo
import com.neuronrobotics.bowlerkernel.gitfs.GitHubFS
import javafx.beans.property.SimpleBooleanProperty
import org.kohsuke.github.GitHub
import tornadofx.*
import java.nio.file.Paths
import javax.inject.Singleton
import kotlin.concurrent.thread

/**
 * Manages logging in and out from GitHub.
 */
@Singleton
class LoginManager {

    private val credentialFile by lazy {
        Paths.get(System.getProperty("user.home"), ".github").toFile()
    }

    val isLoggedInProperty = SimpleBooleanProperty(false)
    var isLoggedIn by isLoggedInProperty

    init {
        isLoggedInProperty.addListener { _, _, new ->
            if (new) {
                thread { cloneAssetRepo() }
            }
        }
    }

    /**
     * Log in using credentials from the default file.
     */
    fun login(): Try<GitHub> {
        return readCredentials().flatMap { it.run { login(first, second) } }
    }

    /**
     * Log in with a [username] and [password].
     */
    fun login(username: String, password: String): Try<GitHub> {
        return Try {
            GitHub.connectUsingPassword(username, password).also {
                getInstanceOf<MainWindowController>().apply {
                    gitHub = Try.just(it)
                    credentials = username to password
                    gitFS = Try.just(GitHubFS(it, credentials))
                }

                writeCredentials(username, password)

                isLoggedIn = true
                LOGGER.info("Logged in $username.")
            }
        }
    }

    /**
     * Logout the currently logged in user.
     */
    fun logout() {
        isLoggedIn = false
        credentialFile.delete()
        LOGGER.info("Logged out.")
    }

    private fun readCredentials(): Try<Pair<String, String>> = Try {
        val (username, password) = credentialFile.readText().split("\n")
        username.trim().removePrefix("login=") to password.trim().removePrefix("password=")
    }

    private fun writeCredentials(username: String, password: String) {
        credentialFile.writeText(
            """
            |login=$username
            |password=$password
            |
            """.trimMargin()
        )
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(LoginManager::class.java.simpleName)
    }
}
