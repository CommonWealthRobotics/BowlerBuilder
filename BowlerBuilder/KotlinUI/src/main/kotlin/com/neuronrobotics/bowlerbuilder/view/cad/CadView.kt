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
class CadView : Fragment() {

    private val controller: CadController by inject()
    val engine: BowlerCadEngine by di()

    override val root = borderpane {
        center = engine

        bottom = hbox {
        }
    }
}
