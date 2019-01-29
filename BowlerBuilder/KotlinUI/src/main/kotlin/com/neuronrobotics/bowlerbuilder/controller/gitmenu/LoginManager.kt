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
import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
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
            return Try.raise(IllegalStateException("Invalid login credentials."))
        }

        return Try {
            GitHub.connectUsingPassword(username, password).also {
                val token = try {
                    it.createToken(
                        setOf(
                            "repo",
                            "gist"
                        ),
                        "BowlerBuilder",
                        ""
                    )
                } catch (ex: HttpException) {
                    LOGGER.warning {
                        """
                        |Failed to generate token:
                        |${Throwables.getStackTraceAsString(ex)}
                        """.trimMargin()
                    }

                    throw ex
                }

                loginToken(username, token.token)
            }
        }.also {
            if (it is HttpException) {
                LOGGER.warning {
                    """
                    |Failed to generate token:
                    |${Throwables.getStackTraceAsString(it)}
                    """.trimMargin()
                }
            }
        }
    }

    /**
     * Log in using a token.
     */
    fun loginToken(username: String, token: String): Try<GitHub> {
        if (precheckCredentials(username, token)) {
            return Try.raise(IllegalStateException("Invalid login credentials."))
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
                    getInstanceOf<MainWindowController>().gitHub = Try.raise(
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

        getInstanceOf<MainWindowController>().gitHub = Try.raise(
            IllegalStateException("User is logged out.")
        )

        LOGGER.info("Logged out.")
    }

    private fun readCredentials(): Try<Pair<String, String>> = Try {
        val (username, password) = credentialFile.readText().split("\n")
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
