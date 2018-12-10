/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder

import com.neuronrobotics.bowlerbuilder.util.Verified
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.sdk.util.ThreadUtil
import org.eclipse.jgit.api.errors.GitAPIException
import org.kohsuke.github.GHGist
import org.kohsuke.github.GHGistBuilder
import java.io.IOException
import java.util.Optional
import java.util.logging.Level

object GistUtilities {

    private val LOGGER = LoggerUtilities.getLogger(GistUtilities::class.java.simpleName)

    /**
     * Create and publish a new gist.
     *
     * @param filename Gist filename for first file
     * @param description Gist description
     * @param isPublic Public/private viewing
     * @return New gist
     */
    @JvmStatic
    fun createNewGist(filename: String, description: String, isPublic: Boolean): Verified<IOException, GHGist> {
        // Setup gist
        val gitHub = ScriptingEngine.getGithub()
        val builder = gitHub.createGist()

        builder.file(filename, "//Your code here")
        builder.description(description)
        builder.public_(isPublic)

        // Make gist
        return createGistFromBuilder(builder, filename)
    }

    /**
     * Create a new Gist.
     *
     * @param builder Gist builder
     * @param filename Gist file filename
     * @return New gist
     */
    private fun createGistFromBuilder(builder: GHGistBuilder, filename: String): Verified<IOException, GHGist> {
        val gist = builder.create()

        while (true) {
            try {
                ScriptingEngine.fileFromGit(gist.gitPullUrl, filename)
                break
            } catch (e: GitAPIException) {
                LOGGER.log(Level.INFO, "Waiting on Git API.")
            } catch (e: IOException) {
                return Verified.error(e)
            }

            ThreadUtil.wait(500)
        }

        return Verified.success(gist)
    }

    /**
     * Validate a file name. A valid file name has an extension.
     *
     * @param fileName File name to validate
     * @return An optional containing a valid file name, empty otherwise
     */
    @JvmStatic
    fun isValidCodeFileName(fileName: String): Optional<String> {
        return if (fileName.matches("^.*\\.[^\\\\]+$".toRegex()) && !fileName.contains(' ')) {
            Optional.of(fileName)
        } else {
            Optional.empty()
        }
    }

    /**
     * Will accept http:// or https:// with .git or .git/.
     *
     * @param url gist URL
     * @return optional containing a valid gist URL, empty otherwise
     */
    @JvmStatic
    fun isValidGitURL(url: String): Optional<String> {
        // Any git URL is ((git|ssh|http(scale)?)|(git@[\w\.]+))(:(//)?)([\w\.@\:/\-~]+)(\.git)(/)?
        return if (url.matches("(http(s)?)(:(//)?)([\\w.@:/\\-~]+)(\\.git)(/)?".toRegex())) {
            Optional.of(url)
        } else {
            Optional.empty()
        }
    }
}
