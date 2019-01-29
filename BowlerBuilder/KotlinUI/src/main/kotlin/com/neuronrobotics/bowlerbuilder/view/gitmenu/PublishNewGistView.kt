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

import arrow.core.Try
import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerkernel.gitfs.GitHubFS
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import tornadofx.*
import java.io.File

/**
 * A form to create a new gist and push the initial commit.
 */
class PublishNewGistView(
    private val scriptContent: String
) : Fragment() {

    private val gistDescriptionProperty = SimpleStringProperty("")
    private var gistDescription by gistDescriptionProperty
    private val gistIsPublicProperty = SimpleBooleanProperty(true)
    private var gistIsPublic by gistIsPublicProperty

    val gistFilenameProperty = SimpleStringProperty("")
    var gistFilename by gistFilenameProperty
    val gitUrlProperty = SimpleStringProperty("")
    var gitUrl by gitUrlProperty
    val publishSuccessfulProperty = SimpleBooleanProperty(false)
    var publishSuccessful by publishSuccessfulProperty
    val publishedFileProperty = SimpleObjectProperty<File>()
    var publishedFile by publishedFileProperty

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

        buttonbar {
            button("Publish") {
                action {
                    runAsync {
                        getInstanceOf<MainWindowController>().gitHub.flatMap {
                            Try {
                                val gist = it.createGist()
                                    .file(gistFilename, scriptContent)
                                    .description(gistDescription)
                                    .public_(gistIsPublic)
                                    .create()

                                publishedFile = GitHubFS.mapGistFileToFileOnDisk(
                                    gist,
                                    gist.getFile(gistFilename)
                                ).fold(
                                    {
                                        throw IllegalStateException(
                                            "Failed to get files in gist.",
                                            it
                                        )
                                    },
                                    { it }
                                )

                                gist
                            }
                        }
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

        fun create(scriptContent: String) = PublishNewGistView(
            scriptContent
        )
    }
}
