/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import tornadofx.*
import java.io.File

class PublishController : Controller() {

    /**
     * Publish updates to a file.
     *
     * @param file The file on disk to the updated [content] to.
     * @param content The new file contents.
     * @param commitMessage The commit message.
     */
    fun publish(
        file: File,
        content: String,
        commitMessage: String
    ) {
        val repo = RepositoryBuilder()
            .findGitDir(file)
            .build()

        file.writeText(content)

        val git = Git(repo)
        git.commit()
            .setMessage(commitMessage)
            .setOnly(file.path)
            .call()

        git.close()
        repo.close()
    }
}
