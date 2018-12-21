package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.view.cad.CadView
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine.BowlerCadEngine
import javafx.geometry.Orientation
import tornadofx.*
import javax.inject.Inject

class CadScriptEditor
@Inject constructor(
    val editor: VisualScriptEditor
) : Fragment() {

    val cadView = CadView()

    override val root = splitpane(
        orientation = Orientation.HORIZONTAL,
        nodes = *arrayOf(editor.root, cadView.root)
    )
}
