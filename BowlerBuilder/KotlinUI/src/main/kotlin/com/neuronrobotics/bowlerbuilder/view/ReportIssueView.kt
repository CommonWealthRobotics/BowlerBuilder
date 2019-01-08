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

class ReportIssueView : Fragment() {

    private val controller = getInstanceOf<ReportIssueController>()
    private val issueTitleProperty = SimpleStringProperty()
    private var issueTitle by issueTitleProperty
    private val issueBodyProperty = SimpleStringProperty()
    private var issueBody by issueBodyProperty
    private val attachLogFileProperty = SimpleBooleanProperty()
    private var attachLogFile by attachLogFileProperty

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            field("Issue Title") {
                textfield(issueTitleProperty)
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
                }
            }

            field("Attach log file (may contain private information)") {
                checkbox(property = attachLogFileProperty)
            }

            buttonbar {
                button("Report").action {
                    runAsync {
                        controller.reportIssue(issueTitle, issueBody, attachLogFile)
                    } success {
                        close()
                    }
                }

                button("Cancel").action {
                    close()
                }
            }
        }
    }
}
