/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview

import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import javafx.scene.Node

interface ScriptEditorView {

    /**
     * Set the font size for the editor.
     *
     * @param fontSize Font size
     */
    fun setFontSize(fontSize: Int)

    /**
     * Get the view for editing the script.
     *
     * @return editor view
     */
    fun getView(): Node

    /**
     * Get the [ScriptEditor] this view interacts with.
     *
     * @return the script editor
     */
    fun getScriptEditor(): ScriptEditor
}
