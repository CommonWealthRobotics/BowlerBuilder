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
        ScriptingEngine.setLoginManager { arrayOf(username, password) }
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
