/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.view.cad.CadView
import javafx.geometry.Orientation
import tornadofx.*
import javax.inject.Inject

class CadScriptEditor
@Inject constructor(
    val editor: VisualScriptEditor
) : Fragment() {

    val cadView = CadView()

    @SuppressWarnings("SpreadOperator")
    override val root = splitpane(
        orientation = Orientation.HORIZONTAL,
        nodes = *arrayOf(editor.root, cadView.root)
    )
}
