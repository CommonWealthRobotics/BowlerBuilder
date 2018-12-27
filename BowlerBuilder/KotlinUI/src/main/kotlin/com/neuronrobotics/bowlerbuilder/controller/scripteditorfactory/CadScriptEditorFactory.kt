/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditor
import java.io.File

interface CadScriptEditorFactory {

    /**
     * Creates a new [CadScriptEditor] and opens it. Do not call from the FX thread.
     *
     * @param url The url of the gist which is being edited.
     * @param file The file on disk where the gist file is cloned.
     * @return The [CadScriptEditor] which was created.
     */
    fun createAndOpenScriptEditor(url: String, file: File): CadScriptEditor

    /**
     * Creates a new scratchpad [CadScriptEditor] and opens it. Do not call from the FX thread.
     *
     * @return The [CadScriptEditor] which was created.
     */
    fun createAndOpenScratchpad(): CadScriptEditor
}
