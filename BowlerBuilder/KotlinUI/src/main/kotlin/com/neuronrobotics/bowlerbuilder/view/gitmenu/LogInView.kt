package com.neuronrobotics.bowlerbuilder.view.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.LoginManager
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class LogInView : Fragment() {

    private val loginManager: LoginManager by di()
    private val usernameProperty = SimpleStringProperty("")
    private val username by usernameProperty
    private val passwordProperty = SimpleStringProperty("")
    private val password by passwordProperty

    override val root = form {
        fieldset("Log In") {
            field("Username") {
                textfield(usernameProperty) {
                    action { tryLogin() }
                }
            }

            field("Password") {
                passwordfield(passwordProperty) {
                    action { tryLogin() }
                }
            }

            buttonbar {
                button("Log In") {
                    action { tryLogin() }
                }

                button("Cancel") {
                    action { close() }
                }
            }
        }
    }

    private fun tryLogin() {
        runAsync {
            loginManager.login(username, password)
        } success {
            if (loginManager.isLoggedIn) {
                close()
            }
        }
    }
}
