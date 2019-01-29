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
