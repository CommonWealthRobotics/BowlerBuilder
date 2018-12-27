/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditor

interface CadScriptEditorFactory {

    /**
     * Creates a new [CadScriptEditor] and opens it.
     *
     * @param gistFile The file to edit.
     *
     * @return The [CadScriptEditor] which was created.
     */
    fun createAndOpenScriptEditor(gistFile: GistFile): CadScriptEditor

    /**
     * Creates a new scratchpad [CadScriptEditor] and opens it.
     *
     * @return The [CadScriptEditor] which was created.
     */
    fun createAndOpenScratchpad(): CadScriptEditor
}
