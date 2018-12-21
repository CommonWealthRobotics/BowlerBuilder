/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.cad

import com.neuronrobotics.bowlerbuilder.controller.cad.CadController
import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine.BowlerCadEngine
import tornadofx.*

/**
 * A view containing the [BowlerCadEngine] and basic engine controls.
 */
class CadView(
    val engine: BowlerCadEngine
) : Fragment() {

    private val controller = CadController()

    override val root = borderpane {
        id = "CadView"
        center = anchorpane(
            nodes = *arrayOf(engine)
        ) {
            val subScene = engine.getSubScene()
            subScene.isFocusTraversable = false
            subScene.widthProperty().bind(this@borderpane.widthProperty())
            subScene.heightProperty().bind(this@borderpane.widthProperty())

            engine.anchorpaneConstraints {
                topAnchor = 0.0
                rightAnchor = 0.0
                leftAnchor = 0.0
                bottomAnchor = 0.0
            }
        }

        bottom = hbox {
        }
    }
}
