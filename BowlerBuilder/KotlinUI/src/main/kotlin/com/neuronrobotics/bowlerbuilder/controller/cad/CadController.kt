/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.cad

import com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine.BowlerCadEngine
import tornadofx.*

class CadController : Controller() {

    val engine = BowlerCadEngine()

    fun homeCamera() = engine.homeCamera()

    fun showHideAxis() {
        engine.axisShowingProperty().value = !engine.axisShowingProperty().value
    }

    fun showHideHand() {
        engine.handShowingProperty().value = !engine.handShowingProperty().value
    }

    fun clearObjects() = engine.clearCSGs()
}
