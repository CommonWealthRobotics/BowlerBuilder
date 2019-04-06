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
package com.neuronrobotics.bowlerbuilder.view.cad

import com.neuronrobotics.bowlerbuilder.controller.cad.CadController
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine.BowlerCadEngine
import javafx.geometry.Insets
import tornadofx.*

/**
 * A view containing the [BowlerCadEngine] and basic engine controls.
 */
class CadView : Fragment() {

    private val controller = CadController()
    val engine
        get() = controller.engine

    @SuppressWarnings("LabeledExpression")
    override val root = borderpane {
        id = "CadView"

        val subScene = engine.getSubScene()
        subScene.isFocusTraversable = false
        subScene.widthProperty().bind(this@borderpane.widthProperty())
        subScene.heightProperty().bind(this@borderpane.heightProperty())
        subScene.anchorpaneConstraints {
            topAnchor = 0.0
            rightAnchor = 0.0
            leftAnchor = 0.0
            bottomAnchor = 0.0
        }

        center = engine.getView()

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true

            button("Home Camera") {
                action {
                    controller.homeCamera()
                }
            }

            button("Show/Hide Axis") {
                action {
                    controller.showHideAxis()
                }
            }

            button("Show/Hide Hand") {
                action {
                    controller.showHideHand()
                }
            }

            button("Clear Objects") {
                action {
                    controller.clearObjects()
                }
            }
        }
    }
}
