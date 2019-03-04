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

        git.pull().call()

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
