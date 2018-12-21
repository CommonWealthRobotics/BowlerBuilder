/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
