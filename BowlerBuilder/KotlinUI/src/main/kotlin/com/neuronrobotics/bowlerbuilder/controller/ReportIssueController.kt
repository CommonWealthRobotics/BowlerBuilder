/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import org.apache.commons.lang3.SystemUtils
import tornadofx.*

class ReportIssueController : Controller() {

    /**
     * Report a new issue to GitHub.
     *
     * @param title The title of the issue.
     * @param text The text body of the issue.
     * @param attachLogFile Whether to attach the current log file to the issue.
     */
    fun reportIssue(title: String, text: String, attachLogFile: Boolean) {
        getInstanceOf<MainWindowController>().gitHub.map {
            val bodyText = text + getIssueBodyFooter(attachLogFile)

            val newIssue = it.getOrganization("CommonWealthRobotics")
                .getRepository("BowlerBuilder")
                .createIssue(title)
                .body(bodyText)
                .create()

            LOGGER.info {
                """
                |Opened issue at:
                |${newIssue.htmlUrl.toExternalForm()}
                """.trimMargin()
            }
        }
    }

    private fun getIssueBodyFooter(attachLogFile: Boolean): String {
        var footer = """
            |
            |
            |BowlerBuilder Version: ${LoggerUtilities.getApplicationVersion()}
            |OS: ${SystemUtils.OS_NAME}, ${SystemUtils.OS_ARCH}, ${SystemUtils.OS_VERSION}
            """.trimMargin()

        if (attachLogFile) {
            footer += "\n<details><summary>Log file:</summary><pre>" +
                LoggerUtilities.readCurrentLogFile() +
                "</pre></details>"
        }

        return footer
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(ReportIssueController::class.java.simpleName)
    }
}
