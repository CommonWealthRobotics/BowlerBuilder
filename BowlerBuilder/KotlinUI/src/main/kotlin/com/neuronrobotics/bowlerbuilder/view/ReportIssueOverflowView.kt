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
