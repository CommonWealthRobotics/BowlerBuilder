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
