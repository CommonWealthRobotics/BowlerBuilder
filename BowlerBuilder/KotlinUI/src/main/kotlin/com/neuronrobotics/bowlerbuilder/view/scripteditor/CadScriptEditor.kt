/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
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
