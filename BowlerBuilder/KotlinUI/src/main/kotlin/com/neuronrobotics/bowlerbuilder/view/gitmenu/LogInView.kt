/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.gitmenu.LoginManager
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/**
 * A form to authenticate a user to GitHub.
 */
class LogInView : Fragment() {

    private val loginManager = getInstanceOf<LoginManager>()
    private val usernameProperty = SimpleStringProperty("")
    private val username by usernameProperty
    private val passwordProperty = SimpleStringProperty("")
    private val password by passwordProperty

    override val root = tabpane {
        tab("Username & Password") {
            form {
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
        }

        tab("Personal Access Token") {
            form {
                fieldset("Log In") {
                    field("Username") {
                        textfield(usernameProperty) {
                            action { tryLoginToken() }
                        }
                    }

                    field("Personal Access Token") {
                        passwordfield(passwordProperty) {
                            action { tryLoginToken() }
                        }
                    }

                    buttonbar {
                        button("Log In") {
                            action { tryLoginToken() }
                        }

                        button("Cancel") {
                            action { close() }
                        }
                    }
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

    private fun tryLoginToken() {
        runAsync {
            loginManager.loginToken(username, password)
        } success {
            if (loginManager.isLoggedIn) {
                close()
            }
        }
    }
}
