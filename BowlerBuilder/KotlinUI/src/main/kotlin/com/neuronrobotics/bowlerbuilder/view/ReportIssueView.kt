/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.controller.ReportIssueController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import tornadofx.*

internal class ReportIssueView
internal constructor() : Fragment() {

    private val controller = getInstanceOf<ReportIssueController>()
    private val issueTitleProperty = SimpleStringProperty()
    private var issueTitle by issueTitleProperty
    private val issueBodyProperty = SimpleStringProperty()
    private var issueBody by issueBodyProperty
    private val attachLogFileProperty = SimpleBooleanProperty()
    private var attachLogFile by attachLogFileProperty
    private val encryptLogFileProperty = SimpleBooleanProperty()
    private var encryptLogFile by encryptLogFileProperty
    private val context = ValidationContext()

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            field("Issue Title") {
                textfield(issueTitleProperty) {
                    context.addValidator(this, this.textProperty()) {
                        if (it.isNullOrBlank() || it.isNullOrEmpty()) {
                            error("Issue title is required")
                        } else {
                            null
                        }
                    }
                }
            }

            field("Issue Description") {
                textarea(issueBodyProperty) {
                    text =
                        """
                        **Describe the bug**
                        A clear and concise description of what the bug is.

                        **Steps To Reproduce**
                        Steps to reproduce the behavior:

                        **Expected behavior**
                        A clear and concise description of what you expected to happen.
                        """.trimIndent()

                    context.addValidator(this, this.textProperty()) {
                        if (it.isNullOrBlank() || it.isNullOrEmpty()) {
                            error("Issue title is required")
                        } else {
                            null
                        }
                    }
                }
            }

            field("Attach log file (may contain private information)") {
                checkbox(property = attachLogFileProperty)
            }

            field("Encrypt log file so only the BowlerBuilder team can read it") {
                checkbox(property = encryptLogFileProperty) {
                    enableWhen(attachLogFileProperty)
                }
            }

            buttonbar {
                button("Report") {
                    enableWhen(context.valid)

                    action {
                        runAsync {
                            controller.reportIssue(
                                issueTitle,
                                issueBody,
                                attachLogFile,
                                encryptLogFile
                            )
                        } success { overflow ->
                            if (overflow != null) {
                                ReportIssueOverflowView(
                                    overflow.second,
                                    overflow.first,
                                    controller
                                ).openModal(block = true)
                            }

                            close()
                        }
                    }
                }

                button("Cancel").action {
                    close()
                }
            }
        }
    }
}
