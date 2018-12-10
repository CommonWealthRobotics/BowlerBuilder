/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor

interface ScriptEditorFactory {

    /**
     * Creates a new [ScriptEditor] and opens it.
     *
     * @param gitUrl The push URL of the script to edit.
     * @param filename The filename of the script to edit.
     *
     * @return The [ScriptEditor] which was created.
     */
    fun createAndOpenScriptEditor(gitUrl: String, filename: String): ScriptEditor

    /**
     * Creates a new scratchpad [ScriptEditor] and opens it.
     *
     * @return The [ScriptEditor] which was created.
     */
    fun createAndOpenScratchpad(): ScriptEditor
}
