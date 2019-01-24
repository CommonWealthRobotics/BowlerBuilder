/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import tornadofx.*
import java.io.File

class PublishController : Controller() {

    /**
     * Publish updates to a file.
     *
     * @param file The file on disk to the updated [content] to.
     * @param commitMessage The commit message.
     */
    fun publish(file: File, commitMessage: String) {
        val repo = RepositoryBuilder()
            .findGitDir(file)
            .build()

        val git = Git(repo)
        git.commit()
            .setAll(true)
            .setMessage(commitMessage)
            .call()

        git.push()
            .setCredentialsProvider(
                getInstanceOf<MainWindowController>().credentials.run {
                    UsernamePasswordCredentialsProvider(first, second)
                }
            )
            .call()

        git.close()
        repo.close()
    }
}
