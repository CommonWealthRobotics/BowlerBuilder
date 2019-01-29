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
                            action { this@LogInView.close() }
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
                            action { this@LogInView.close() }
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
