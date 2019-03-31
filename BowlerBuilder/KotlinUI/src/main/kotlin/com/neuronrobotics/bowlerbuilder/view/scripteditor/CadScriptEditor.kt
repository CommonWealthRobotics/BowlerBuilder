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
import javafx.scene.control.SplitPane
import tornadofx.*
import javax.inject.Inject

@SuppressWarnings("SpreadOperator")
class CadScriptEditor
@Inject constructor(
    val editor: VisualScriptEditor
) : Fragment() {

    private var regenRoot = false
    var cadView = CadView()

    private var lastRoot: SplitPane = splitpane(
        orientation = Orientation.HORIZONTAL,
        nodes = *arrayOf(editor.root, cadView.root)
    )

    override val root: SplitPane
        get() {
            if (regenRoot) {
                regenRoot = false

                lastRoot = splitpane(
                    orientation = Orientation.HORIZONTAL,
                    nodes = *arrayOf(editor.root, cadView.root)
                )
            }

            return lastRoot
        }

    /**
     * Sets a flag that the next time [root] is accessed it should be regenerated so that the
     * [newCadView] is used instead of the old one.
     */
    fun setRegenerateRoot(newCadView: CadView) {
        regenRoot = true
        cadView = newCadView
    }
}
