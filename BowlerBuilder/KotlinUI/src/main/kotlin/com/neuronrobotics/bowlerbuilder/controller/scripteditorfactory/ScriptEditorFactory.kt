/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor

interface ScriptEditorFactory {

    /**
     * Creates a new [ScriptEditor] and opens it.
     *
     * @param gistFile The file to edit.
     *
     * @return The [ScriptEditor] which was created.
     */
    fun createAndOpenScriptEditor(gistFile: GistFile): ScriptEditor

    /**
     * Creates a new scratchpad [ScriptEditor] and opens it.
     *
     * @return The [ScriptEditor] which was created.
     */
    fun createAndOpenScratchpad(): ScriptEditor
}
