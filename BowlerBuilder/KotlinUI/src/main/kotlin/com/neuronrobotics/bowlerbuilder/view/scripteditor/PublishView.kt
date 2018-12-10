package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.PublishController
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*

class PublishView : Fragment() {

    private val controller: PublishController by inject()

    override val root = form {
        lateinit var commitMessageField: Field
        fieldset("Commit", labelPosition = Orientation.VERTICAL) {
            commitMessageField = field("Commit Message", Orientation.VERTICAL) {
                textarea {
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
                            commitMessage = commitMessageField.text
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
