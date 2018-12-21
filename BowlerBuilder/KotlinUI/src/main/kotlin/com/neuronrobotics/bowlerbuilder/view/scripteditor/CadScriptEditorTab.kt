/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import javafx.scene.control.Tab

class CadScriptEditorTab(
    text: String,
    visualScriptEditor: VisualScriptEditor
) : Tab(text) {

    val editor: CadScriptEditor = CadScriptEditor(visualScriptEditor)

    init {
        content = editor.root
    }
}
