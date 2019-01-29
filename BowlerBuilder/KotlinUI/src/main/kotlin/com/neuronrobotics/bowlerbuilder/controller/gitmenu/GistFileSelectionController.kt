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

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import javax.inject.Inject

class GistFileSelectionController
@Inject constructor(
    private val cadScriptEditorFactory: CadScriptEditorFactory
) : Controller() {

    val filesInGist: ObservableList<File> = FXCollections.observableArrayList<File>()

    /**
     * Loads the files in a gist by [gistUrl] into [filesInGist].
     */
    fun loadFilesInGist(gistUrl: String) {
        LOGGER.info("Loading files for: $gistUrl")
        getInstanceOf<MainWindowController>().gitFS.flatMap {
            it.cloneRepoAndGetFiles(gistUrl)
        }.toEither().bimap(
            {
                LOGGER.warning(
                    """
                    |Failed to clone repo from: $gistUrl
                    |${it.localizedMessage}
                    """.trimMargin()
                )

                LOGGER.fine(
                    """
                    |Failed to clone repo from: $gistUrl
                    |${Throwables.getStackTraceAsString(it)}
                    """.trimMargin()
                )
            },
            {
                filesInGist.setAll(it)
            }
        )
    }

    /**
     * Opens a [file] with the injected [CadScriptEditorFactory].
     */
    fun openGistFile(url: String, file: File) {
        cadScriptEditorFactory.createAndOpenScriptEditor(url, file)
    }

    companion object {
        private val LOGGER =
            LoggerUtilities.getLogger(GistFileSelectionController::class.java.simpleName)
    }
}
