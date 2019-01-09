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
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.Security

class ReportIssueController : Controller() {

    /**
     * Report a new issue to GitHub.
     *
     * @param title The title of the issue.
     * @param text The text body of the issue.
     * @param attachLogFile Whether to attach the current log file to the issue.
     */
    fun reportIssue(
        title: String,
        text: String,
        attachLogFile: Boolean,
        encryptLogFile: Boolean
    ) {
        getInstanceOf<MainWindowController>().gitHub.map {
            val bodyText = text + getIssueBodyFooter(attachLogFile, encryptLogFile)

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

    private fun getIssueBodyFooter(attachLogFile: Boolean, encryptLogFile: Boolean): String {
        var footer = """
            |
            |
            |BowlerBuilder Version: ${LoggerUtilities.getApplicationVersion()}
            |OS: ${SystemUtils.OS_NAME}, ${SystemUtils.OS_ARCH}, ${SystemUtils.OS_VERSION}
            """.trimMargin()

        if (attachLogFile) {
            val logFileContent = if (encryptLogFile) {
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

            footer += "\n<details><summary>Log file:</summary><pre>" +
                logFileContent +
                "</pre></details>"
        }

        return footer
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(ReportIssueController::class.java.simpleName)
    }
}
