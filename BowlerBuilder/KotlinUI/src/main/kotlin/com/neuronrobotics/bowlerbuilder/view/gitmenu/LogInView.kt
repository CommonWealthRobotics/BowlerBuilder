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
                textfield(usernameProperty)
            }

            field("Password") {
                passwordfield(passwordProperty)
            }

            buttonbar {
                button("Log In") {
                    action {
                        runAsync {
                            loginManager.login(username, password)
                        } success {
                            if (loginManager.isLoggedIn) {
                                close()
                            }
                        }
                    }
                }

                button("Cancel") {
                    action { close() }
                }
            }
        }
    }
}
