/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*
import java.io.IOException
import javax.inject.Singleton

@Singleton
class LoginManager {

    val isLoggedInProperty = SimpleBooleanProperty(false)
    var isLoggedIn by isLoggedInProperty

    init {
        isLoggedIn = try {
            ScriptingEngine.runLogin()
            ScriptingEngine.isLoginSuccess() && ScriptingEngine.hasNetwork()
        } catch (ex: IOException) {
            false
        }
    }

    fun login(username: String, password: String) {
        var failedLastTry = false
        ScriptingEngine.setLoginManager {
            if (!failedLastTry) {
                failedLastTry = true
                arrayOf(username, password)
            } else {
                arrayOf("", "")
            }
        }

        ScriptingEngine.waitForLogin()

        if (ScriptingEngine.isLoginSuccess() && ScriptingEngine.hasNetwork()) {
            isLoggedIn = true
            LOGGER.info {
                "Login as $username successful."
            }
        } else {
            isLoggedIn = false
            LOGGER.severe {
                "Login as $username not successful."
            }
        }
    }

    fun logout() {
        ScriptingEngine.logout()
        isLoggedIn = false
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(LoginManager::class.java.simpleName)
    }
}
