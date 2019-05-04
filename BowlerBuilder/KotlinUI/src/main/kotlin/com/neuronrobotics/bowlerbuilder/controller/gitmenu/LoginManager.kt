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
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import arrow.core.Try
import arrow.core.Try.Companion.raiseError
import arrow.core.extensions.`try`.monadThrow.bindingCatch
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.util.getNonLoopbackNIMacs
import com.neuronrobotics.bowlerbuilder.controller.util.severeShort
import com.neuronrobotics.bowlerkernel.gitfs.GitHubFS
import javafx.beans.property.SimpleBooleanProperty
import org.kohsuke.github.GitHub
import org.kohsuke.github.HttpException
import tornadofx.*
import java.nio.file.Paths
import javax.inject.Singleton

/**
 * Manages logging in and out from GitHub.
 */
@Singleton
class LoginManager {

    private val credentialFile by lazy {
        Paths.get(System.getProperty("user.home"), ".bowler-github").toFile()
    }

    val isLoggedInProperty = SimpleBooleanProperty(false)
    var isLoggedIn by isLoggedInProperty

    /**
     * Log in using the token from the default file.
     */
    fun login(): Try<GitHub> = readCredentials().flatMap { it.run { loginToken(first, second) } }

    /**
     * Log in with a [username] and [password]. Creates a token with repo and gist scopes.
     */
    fun login(username: String, password: String): Try<GitHub> {
        if (precheckCredentials(username, password)) {
            return raiseError(IllegalStateException("Invalid login credentials."))
        }

        val tokenGitHub = bindingCatch {
            val gitHub = GitHub.connectUsingPassword(username, password)
            val mac = getNonLoopbackNIMacs().firstOrNull() ?: "unknown-mac"
            val token = gitHub.createToken(setOf("repo", "gist"), "BowlerBuilder-$mac", "")
            val (tokenGitHub) = loginToken(username, token.token)
            tokenGitHub
        }

        when (tokenGitHub) {
            is Try.Failure -> {
                LOGGER.severeShort(tokenGitHub.exception) {
                    val message = when (val ex = tokenGitHub.exception) {
                        is HttpException -> "${ex.responseCode} ${ex.responseMessage}"
                        else -> ex.localizedMessage
                    }

                    """
                    |Failed to generate token:
                    |$message
                    """.trimMargin()
                }
            }
        }

        return tokenGitHub
    }

    /**
     * Log in using a token.
     */
    fun loginToken(username: String, token: String): Try<GitHub> {
        if (precheckCredentials(username, token)) {
            return raiseError(IllegalStateException("Invalid login credentials."))
        }

        return Try {
            GitHub.connectUsingOAuth(token).also {
                if (it.isCredentialValid) {
                    writeCredentials(username, token)
                    getInstanceOf<MainWindowController>().apply {
                        gitHub = Try.just(it)
                        credentials = username to token
                        gitFS = Try.just(GitHubFS(it, credentials))
                    }

                    isLoggedIn = true
                    LOGGER.info { "Logged in." }
                } else {
                    getInstanceOf<MainWindowController>().gitHub = raiseError(
                        IllegalStateException("Invalid login credentials.")
                    )

                    LOGGER.warning { "Failed to log in to GitHub." }
                }
            }
        }
    }

    private fun precheckCredentials(username: String, token: String) =
        username.isEmpty() || token.isEmpty()

    /**
     * Logout the currently logged in user.
     */
    fun logout() {
        isLoggedIn = false
        credentialFile.delete()

        getInstanceOf<MainWindowController>().gitHub = raiseError(
            IllegalStateException("User is logged out.")
        )

        LOGGER.info { "Logged out." }
    }

    private fun readCredentials(): Try<Pair<String, String>> =
        Try { credentialFile.readText() }.map {
            val (username, password) = it.split("\n")
            username.trim().removePrefix("username=") to password.trim().removePrefix("token=")
        }

    private fun writeCredentials(username: String, token: String) {
        credentialFile.writeText(
            """
            |username=$username
            |token=$token
            |
            """.trimMargin()
        )
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(LoginManager::class.java.simpleName)
    }
}
