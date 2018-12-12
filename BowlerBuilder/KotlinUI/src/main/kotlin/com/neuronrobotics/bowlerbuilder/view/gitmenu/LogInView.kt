/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.gitmenu.LoginManager
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/**
 * A form to authenticate a user to GitHub.
 */
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
