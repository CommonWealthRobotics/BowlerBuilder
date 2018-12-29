/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.util

import arrow.core.Try
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.util.BOWLERBUILDER_DIRECTORY
import com.neuronrobotics.bowlerkernel.util.GIT_CACHE_DIRECTORY
import com.neuronrobotics.kinematicschef.util.toImmutableList
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GHGist
import org.kohsuke.github.GHGistFile
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import java.io.File
import java.nio.file.Paths

const val BOWLER_ASSET_REPO = "https://github.com/madhephaestus/BowlerStudioImageAssets.git"

/**
 * Loads an asset from the [BOWLER_ASSET_REPO].
 *
 * @param filename The name of the file in the repo.
 * @return The asset file.
 */
fun loadBowlerAsset(credentials: Pair<String, String>, filename: String): Try<File> =
    cloneRepoAndGetFiles(
        credentials,
        BOWLER_ASSET_REPO
    ).flatMap {
        Try { it.first { it.name == filename } }
    }

/**
 * Clones the [BOWLER_ASSET_REPO].
 */
fun cloneAssetRepo(credentials: Pair<String, String>) {
    cloneRepo(
        credentials,
        BOWLER_ASSET_REPO
    )
}

/**
 * Maps a [gistUrl] to its id.
 *
 * @param gistUrl The gist URL, i.e.
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 */
fun gistUrlToGistId(gistUrl: String): String =
    gistUrl
        .removePrefix("http://github.com/")
        .removePrefix("https://github.com/")
        .removePrefix("http://gist.github.com/")
        .removePrefix("https://gist.github.com/")
        .removeSuffix(".git")

/**
 * Maps a [gitUrl] to its directory on disk. The directory does not necessarily exist.
 *
 * @param gitUrl The `.git` URL to clone from, i.e.
 * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 */
@SuppressWarnings("SpreadOperator")
fun gitUrlToDirectory(gitUrl: String): File {
    val subDirs = gitUrl
        .removePrefix("http://github.com/")
        .removePrefix("https://github.com/")
        .removePrefix("http://gist.github.com/")
        .removePrefix("https://gist.github.com/")
        .removeSuffix(".git")
        .split("/")

    return Paths.get(
        System.getProperty("user.home"),
        BOWLERBUILDER_DIRECTORY,
        GIT_CACHE_DIRECTORY,
        *subDirs.toTypedArray()
    ).toFile()
}

/**
 * Maps a file in a gist to its file on disk. Fails if the file is not on disk.
 *
 * @param gist The gist.
 * @param gistFile The file in the gist.
 * @return The file on disk.
 */
fun mapGistFileToFileOnDisk(gist: GHGist, gistFile: GHGistFile): Try<File> {
    val directory =
        gitUrlToDirectory(gist.gitPullUrl)

    return Try {
        directory.walkTopDown().first { it.name == gistFile.fileName }
    }
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
fun cloneRepoAndGetFiles(
    credentials: Pair<String, String>,
    gitUrl: String,
    branch: String = "HEAD"
): Try<ImmutableList<File>> =
    cloneRepo(credentials, gitUrl, branch).map {
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
    return if (isValidHttpGitURL(gitUrl)) {
        val directory = gitUrlToDirectory(gitUrl)
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
 * Forks a gist.
 *
 * @param gitHub The GitHub to fork with.
 * @param gistId The id of the gist to fork.
 * @return The fork of the gist.
 */
fun forkGist(
    gitHub: GitHub,
    gistId: String
): Try<GHGist> {
    return Try {
        gitHub.getGist(gistId).fork()
    }
}

/**
 * Forks a repository.
 *
 * @param gitHub The GitHub to fork with.
 * @param repoOwner The name of the repository owner.
 * @param repoName The name of the repository.
 * @return The fork of the repository.
 */
fun forkRepo(
    gitHub: GitHub,
    repoOwner: String,
    repoName: String
) = forkRepo(gitHub, "$repoOwner/$repoName")

/**
 * Forks a repository.
 *
 * @param gitHub The GitHub to fork with.
 * @param repoFullName The full name (owner and repository) of the repository, i.e.
 * `OwnerName/RepoName`.
 * @return The fork of the repository.
 */
fun forkRepo(
    gitHub: GitHub,
    repoFullName: String
): Try<GHRepository> {
    return Try {
        gitHub.getRepository(repoFullName).fork()
    }
}

/**
 * Returns whether the [url] is a valid HTTP Git url.
 *
 * @param url The url to validate
 * @return Whether the [url] is a valid HTTP Git url.
 */
fun isValidHttpGitURL(url: String) =
    url.matches("(http(s)?)(:(//)?)([\\w.@:/\\-~]+)(\\.git)(/)?".toRegex())
