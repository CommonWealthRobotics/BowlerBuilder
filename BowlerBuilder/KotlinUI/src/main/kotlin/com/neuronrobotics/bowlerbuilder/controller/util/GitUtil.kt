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
    getInstanceOf<MainWindowController>().let { controller ->
        controller.ideAction("Cloning $BOWLER_ASSET_REPO") {
            controller.gitFS.flatMap {
                it.cloneRepoAndGetFiles(BOWLER_ASSET_REPO).flatMap {
                    Try { it.first { it.name == filename } }
                }
            }
        }
    }

/**
 * Clones the [BOWLER_ASSET_REPO].
 */
fun cloneAssetRepo() {
    getInstanceOf<MainWindowController>().let { controller ->
        controller.ideAction("Cloning $BOWLER_ASSET_REPO") {
            controller.gitFS.flatMap {
                it.cloneRepo(BOWLER_ASSET_REPO)
            }
        }
    }
}
