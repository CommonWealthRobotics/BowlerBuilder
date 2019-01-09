/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.controller.ReportIssueController
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import javafx.geometry.Orientation
import tornadofx.*
import java.nio.file.Paths

internal class ReportIssueOverflowView
internal constructor(
    link: String,
    content: String,
    controller: ReportIssueController
) : Fragment() {

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            field("Please attach the log file manually:") {
                textarea {
                    text = content
                    isEditable = false
                }
            }

            field("Issue Link:") {
                hyperlink(link).action {
                    controller.openUrl(link)
                }
            }

            buttonbar {
                button("Save to File").action {
                    chooseDirectory(
                        "Select where to save the log file."
                    )?.let {
                        runAsync {
                            Paths.get(
                                it.absolutePath,
                                LoggerUtilities.currentLogFileName()
                            ).toFile().writeText(content)
                        }
                    }
                }

                button("Close").action {
                    close()
                }
            }
        }
    }
}
