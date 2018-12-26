/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.util.filesInRepo
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.model.GistFile
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
        filesInRepo(
            getInstanceOf<MainWindowController>().credentials,
            gistUrl
        ).toEither().bimap(
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
     * Opens a [gistFile] with the injected [CadScriptEditorFactory].
     */
    fun openGistFile(gistFile: GistFile) {
        cadScriptEditorFactory.createAndOpenScriptEditor(gistFile)
    }

    companion object {
        private val LOGGER =
            LoggerUtilities.getLogger(GistFileSelectionController::class.java.simpleName)
    }
}
