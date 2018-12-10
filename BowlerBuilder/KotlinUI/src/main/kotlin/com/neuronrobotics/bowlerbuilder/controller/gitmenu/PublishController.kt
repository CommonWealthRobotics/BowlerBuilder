/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import tornadofx.*

class PublishController : Controller() {

    /**
     * Publish updates to a file.
     *
     * @param gitUrl The push URL.
     * @param filename The file name in the remote repo.
     * @param fileContent The new file content.
     * @param commitMessage The commit message.
     */
    fun publish(
        gitUrl: String,
        filename: String,
        fileContent: String,
        commitMessage: String
    ) {
        val file = ScriptingEngine.fileFromGit(gitUrl, filename)
        val git = ScriptingEngine.locateGit(file)
        val remote = git.repository.config.getString("remote", "origin", "url")

        ScriptingEngine.pushCodeToGit(
            remote,
            ScriptingEngine.getFullBranch(remote),
            ScriptingEngine.findLocalPath(file, git),
            fileContent,
            commitMessage
        )
    }
}
