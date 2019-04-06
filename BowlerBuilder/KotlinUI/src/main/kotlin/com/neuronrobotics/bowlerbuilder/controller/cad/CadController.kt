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
