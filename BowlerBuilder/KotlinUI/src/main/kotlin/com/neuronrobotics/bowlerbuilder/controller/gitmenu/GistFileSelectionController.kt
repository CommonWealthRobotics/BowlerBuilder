/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.cloneRepo
import com.neuronrobotics.bowlerbuilder.controller.filesInRepo
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.util.toImmutableList
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.nio.file.Files
import javax.inject.Inject

class GistFileSelectionController
@Inject constructor(
    private val cadScriptEditorFactory: CadScriptEditorFactory
) : Controller() {

    val filesInGist: ObservableList<String> = FXCollections.observableArrayList<String>()

    /**
     * Loads the files in a gist by [gistUrl] into [filesInGist].
     */
    fun loadFilesInGist(gistUrl: String) {
        LOGGER.info("Loading files for: $gistUrl")
        filesInRepo(gistUrl).toEither().bimap(
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
                filesInGist.setAll(it.map { it.name })
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
