/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.gitmenu

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.GistUtilities
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.gitmenu.PublishController
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class PublishNewGistView : Fragment() {

    private val controller: PublishController by inject()

    private val commitMessageProperty = SimpleStringProperty("")
    private var commitMessage by commitMessageProperty
    val gistFilenameProperty = SimpleStringProperty("")
    var gistFilename by gistFilenameProperty
    private val gistDescriptionProperty = SimpleStringProperty("")
    private var gistDescription by gistDescriptionProperty
    private val gistIsPublicProperty = SimpleBooleanProperty(true)
    private var gistIsPublic by gistIsPublicProperty
    val gitUrlProperty = SimpleStringProperty("")
    var gitUrl by gitUrlProperty
    val publishSuccessfulProperty = SimpleBooleanProperty(false)
    var publishSuccessful by publishSuccessfulProperty

    override val root = form {
        fieldset("Gist Configuration", labelPosition = Orientation.VERTICAL) {
            field("Filename", Orientation.VERTICAL) {
                textfield(gistFilenameProperty)
            }

            field("Description", Orientation.VERTICAL) {
                textfield(gistDescriptionProperty)
            }

            field("Public", Orientation.VERTICAL) {
                checkbox(property = gistIsPublicProperty)
            }
        }

        fieldset("Commit", labelPosition = Orientation.VERTICAL) {
            field("Commit Message", Orientation.VERTICAL) {
                textarea(commitMessageProperty) {
                    prefRowCount = 3
                    vgrow = Priority.ALWAYS
                }
            }
        }

        buttonbar {
            button("Publish") {
                action {
                    runAsync {
                        GistUtilities.createNewGist(
                            gistFilename,
                            gistDescription,
                            gistIsPublic
                        ).handle(
                            {
                                gitUrl = it.gitPushUrl
                                controller.publish(
                                    gitUrl = it.gitPushUrl,
                                    filename = gistFilename,
                                    fileContent = params["file_content"] as String,
                                    commitMessage = commitMessage
                                )
                            },
                            {
                                throw it
                            }
                        )
                    } success {
                        publishSuccessful = true
                        close()
                    } fail {
                        publishSuccessful = false
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
        private val LOGGER = LoggerUtilities.getLogger(PublishNewGistView::class.java.simpleName)
    }
}
