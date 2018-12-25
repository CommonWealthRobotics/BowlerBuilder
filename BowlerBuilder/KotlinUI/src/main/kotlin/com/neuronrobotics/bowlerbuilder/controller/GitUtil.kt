/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import arrow.core.Try
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.GistUtilities
import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.kinematicschef.util.toImmutableList
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.jlleitschuh.guice.key
import java.io.File
import java.nio.file.Paths

const val BOWLER_ASSET_REPO = "https://github.com/madhephaestus/BowlerStudioImageAssets.git"

/**
 * Loads an asset from the [BOWLER_ASSET_REPO].
 *
 * @param filename The name of the file in the repo.
 * @return The asset file.
 */
fun loadBowlerAsset(filename: String): Try<File> =
    filesInRepo(BOWLER_ASSET_REPO).flatMap {
        Try { it.first { it.name == filename } }
    }

/**
 * Clones the [BOWLER_ASSET_REPO].
 */
fun cloneAssetRepo(credentials: Pair<String, String>) {
    cloneRepo(credentials, BOWLER_ASSET_REPO)
}

/**
 * Clones a repository and returns the files in it, excluding the `.git` files.
 *
 * @param gitUrl The `.git` URL to clone from, i.e.
 * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 * @param branch The branch to checkout.
 * @return The files in the repository.
 */
fun filesInRepo(gitUrl: String, branch: String = "HEAD"): Try<ImmutableList<File>> =
    cloneRepo(gitUrl, branch).map {
        it.walkTopDown()
            .filter { file -> file.path != it.path }
            .filter { !it.path.contains(".git") }
            .toList()
            .toImmutableList()
    }

/**
 * Clones a repository to the local cache.
 *
 * @param credentials The credentials to authenticate to GitHub with.
 * @param gitUrl The `.git` URL to clone from, i.e.
 * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 * @param branch The branch to checkout.
 * @return The directory of the cloned repository.
 */
fun cloneRepo(
    credentials: Pair<String, String>,
    gitUrl: String,
    branch: String = "HEAD"
): Try<File> {
    return if (GistUtilities.isValidGitURL(gitUrl).isPresent) {
        val subDirs = gitUrl
            .removePrefix("http://github.com/")
            .removePrefix("https://github.com/")
            .removePrefix("http://gist.github.com/")
            .removePrefix("https://gist.github.com/")
            .removeSuffix(".git")
            .split("/")

        val directory = Paths.get(
            System.getProperty("user.home"),
            MainWindowController.BOWLERBUILDER_DIRECTORY,
            "git-cache",
            *subDirs.toTypedArray()
        ).toFile()

        if (directory.mkdirs()) {
            // If true, the directories were created which means a new repository is
            // being cloned
            Try {
                Git.cloneRepository()
                    .setURI(gitUrl)
                    .setBranch(branch)
                    .setDirectory(directory)
                    .setCredentialsProvider(
                        UsernamePasswordCredentialsProvider(
                            credentials.first,
                            credentials.second
                        )
                    )
                    .call()
                    .close()
            }.map {
                directory
            }
        } else {
            // If false, the repository is already cloned, so pull instead
            Try {
                Git.open(directory).pull().call()
            }.map {
                directory
            }
        }
    } else {
        Try.raise(
            IllegalArgumentException(
                """
                            |Invalid git URL:
                            |$gitUrl
                            """.trimMargin()
            )
        )
    }
}

/**
 * Clones a repository to the local cache.
 *
 * @param gitUrl The `.git` URL to clone from, i.e.
 * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 * @param branch The branch to checkout.
 * @return The directory of the cloned repository.
 */
fun cloneRepo(gitUrl: String, branch: String = "HEAD"): Try<File> =
    cloneRepo(
        MainWindowView.injector.getInstance(key<MainWindowController>()).credentials,
        gitUrl,
        branch
    )
