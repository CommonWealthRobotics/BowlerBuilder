/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.util

import arrow.core.Try
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import java.io.File

const val BOWLER_ASSET_REPO = "https://github.com/madhephaestus/BowlerStudioImageAssets.git"

/**
 * Loads an asset from the [BOWLER_ASSET_REPO].
 *
 * @param filename The name of the file in the repo.
 * @return The asset file.
 */
fun loadBowlerAsset(filename: String): Try<File> =
    getInstanceOf<MainWindowController>().gitFS.flatMap {
        it.cloneRepoAndGetFiles(BOWLER_ASSET_REPO).flatMap {
            Try { it.first { it.name == filename } }
        }
    }

/**
 * Clones the [BOWLER_ASSET_REPO].
 */
fun cloneAssetRepo() {
    getInstanceOf<MainWindowController>().gitFS.flatMap {
        it.cloneRepo(BOWLER_ASSET_REPO)
    }
}
