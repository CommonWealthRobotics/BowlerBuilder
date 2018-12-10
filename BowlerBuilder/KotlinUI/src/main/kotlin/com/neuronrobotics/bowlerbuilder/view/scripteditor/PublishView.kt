/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.PublishController
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class PublishView : Fragment() {

    private val controller: PublishController by inject()
    private val commitMessageProperty = SimpleStringProperty("")
    private var commitMessage by commitMessageProperty

    override val root = form {
        fieldset("Commit", labelPosition = Orientation.VERTICAL) {
            field("Commit Message", Orientation.VERTICAL) {
                textarea(commitMessageProperty) {
                    prefRowCount = 3
                    vgrow = Priority.ALWAYS
                }
            }
        }

        buttonbar {
            button("Push") {
                action {
                    runAsync {
                        controller.publish(
                            gitUrl = params["git_url"] as String,
                            filename = params["filename"] as String,
                            fileContent = params["file_content"] as String,
                            commitMessage = commitMessage
                        )
                    } success {
                        close()
                    } fail {
                        LOGGER.severe {
                            """
                            |Failed to push!
                            |${Throwables.getStackTraceAsString(it)}
                            """.trimMargin()
                        }
                        close()
                    }
                }
            }

            button("Cancel") { action { close() } }
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(PublishView::class.java.simpleName)
    }
}
