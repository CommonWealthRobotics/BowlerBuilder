/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*

class GistFileSelectionController : Controller() {

    private val scriptEditorFactory: ScriptEditorFactory by di()
    val filesInGist: ObservableList<String> = FXCollections.observableArrayList<String>()

    fun loadFilesInGist(gistUrl: String) {
        try {
            ScriptingEngine.filesInGit(gistUrl)
        } catch (ex: IndexOutOfBoundsException) {
            // This is the ScriptingEngine getting an invalid url
            emptyList<String>()
        }.let {
            filesInGist.setAll(it)
        }
    }

    fun openGistFile(gistFile: GistFile) {
        scriptEditorFactory.createAndOpenScriptEditor(gistFile)
    }
}
