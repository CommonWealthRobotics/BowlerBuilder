/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import arrow.core.Try
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.util.cloneAssetRepo
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

    private var github: Try<GitHub>? = null
    private var credentials: Pair<String, String>? = null
    private val credentialFile by lazy {
        Paths.get(System.getProperty("user.home"), ".github").toFile()
    }

    val isLoggedInProperty = SimpleBooleanProperty(false)
    var isLoggedIn by isLoggedInProperty

    init {
        isLoggedInProperty.addListener { _, _, new ->
            if (new) {
                credentials?.let {
                    thread { cloneAssetRepo(it) }
                }
            }
        }

        github = login()
    }

    /**
     * Log in using credentials from the default file or the environment.
     */
    fun login(): Try<GitHub> {
        return Try {
            GitHub.connect().also {
                isLoggedIn = true
                LOGGER.info("Logged in.")
            }
        }
    }

    /**
     * Log in using an OAuth token.
     */
    fun login(oauthToken: String): Try<GitHub> {
        return Try {
            GitHub.connectUsingOAuth(oauthToken).also {
                isLoggedIn = true
                LOGGER.info("Logged in.")
            }
        }
    }

    /**
     * Log in with a [username] and [password].
     */
    fun login(username: String, password: String): Try<GitHub> {
        return Try {
            GitHub.connectUsingPassword(username, password).also {
                isLoggedIn = true
                credentials = username to password
                writeCredentials(username, password)
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
