/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs
import org.apache.commons.lang3.SystemUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.Streams
import tornadofx.*
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.security.Security

internal class ReportIssueController
internal constructor() : Controller() {

    /**
     * Report a new issue to GitHub.
     *
     * @param title The title of the issue.
     * @param text The text body of the issue.
     * @param attachLogFile Whether to attach the current log file to the issue.
     * @return The log file contents to attach manually and the issue URL, `null` if the log file
     * was put in the issue body.
     */
    internal fun reportIssue(
        title: String,
        text: String,
        attachLogFile: Boolean,
        encryptLogFile: Boolean
    ): Pair<String, String>? {
        return getInstanceOf<MainWindowController>().gitHub.fold(
            { null },
            {
                val footer = getIssueBodyFooter()
                val logFile = if (attachLogFile) getLogFile(encryptLogFile) else ""
                val wrappedLogFile = wrapLogFileForGitHub(logFile)

                val (bodyText, overflowLogFile) =
                    if (text.length + footer.length + wrappedLogFile.length
                        >= GITHUB_ISSUE_MAX_LENGTH
                    ) {
                        LOGGER.info {
                            "Issue body is too long, the log file must be attached manually."
                        }

                        text to logFile
                    } else {
                        (text + footer + wrappedLogFile) to null
                    }

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

                return if (overflowLogFile != null) {
                    overflowLogFile to newIssue.htmlUrl.toExternalForm()
                } else {
                    null
                }
            }
        )
    }

    internal fun openUrl(url: String) {
        Desktop.getDesktop().browse(URL(url).toURI())
    }

    private fun getIssueBodyFooter(): String = """
        |
        |
        |BowlerBuilder Version: ${LoggerUtilities.getApplicationVersion()}
        |OS: ${SystemUtils.OS_NAME}, ${SystemUtils.OS_ARCH}, ${SystemUtils.OS_VERSION}
        """.trimMargin()

    private fun wrapLogFileForGitHub(logFile: String) =
        "\n<details><summary>Logfile:</summary><pre>$logFile</pre></details>"

    private fun getLogFile(encryptLogFile: Boolean): String = if (encryptLogFile) {
        val keyfile = File(
            ReportIssueController::class.java
                .getResource("../bowlerbuilderteam-public.gpg").toURI()
        )

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }

        val encryptedLogFileStream = ByteArrayOutputStream()

        BouncyGPG.encryptToStream()
            .withConfig(
                KeyringConfigs.forGpgExportedKeys(
                    KeyringConfigCallbacks.withUnprotectedKeys()
                ).apply {
                    addPublicKey(keyfile.readBytes())
                }
            )
            .withStrongAlgorithms()
            .toRecipient("kharrington@commonwealthrobotics.com")
            .andDoNotSign()
            .armorAsciiOutput()
            .andWriteTo(encryptedLogFileStream).use {
                Streams.pipeAll(LoggerUtilities.currentLogFileStream(), it)
            }

        encryptedLogFileStream.toString()
    } else {
        LoggerUtilities.readCurrentLogFile()
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(ReportIssueController::class.java.simpleName)
        private const val GITHUB_ISSUE_MAX_LENGTH = 65536
    }
}
