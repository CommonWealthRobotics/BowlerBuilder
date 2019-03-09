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

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.gitmenu.PublishController
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File

/**
 * A form to commit and push changes to a file.
 */
class PublishView(
    private val file: File
) : Fragment() {

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
                    this@form.runAsyncWithOverlay(
                        overlayNode = MaskPane().apply {
                            center = progressindicator {
                                scaleX = 0.5
                                scaleY = 0.5
                            }
                        }
                    ) {
                        controller.publish(file, commitMessage)
                    } success {
                        LOGGER.info { "Push successful." }
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
