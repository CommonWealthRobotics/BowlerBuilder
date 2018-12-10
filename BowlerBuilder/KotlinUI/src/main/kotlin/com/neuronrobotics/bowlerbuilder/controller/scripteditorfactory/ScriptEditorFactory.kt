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
}
