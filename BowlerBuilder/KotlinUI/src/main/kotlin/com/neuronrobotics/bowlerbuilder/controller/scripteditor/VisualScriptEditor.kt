package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import javafx.scene.Parent

/**
 * A [ScriptEditor] which can also be interfaces through visually using the [root] node.
 */
interface VisualScriptEditor : ScriptEditor {
    val root: Parent
}
