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
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.camera

import javafx.scene.Group
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import javafx.scene.transform.Translate

/**
 * A [Group] which can be translated, rotated (ZYX), and scaled.
 */
class TransformableGroup : Group() {

    val translate = Translate()
    val rotX = Rotate()
    val rotY = Rotate()
    val rotZ = Rotate()
    val scale = Scale()

    init {
        rotX.axis = Rotate.X_AXIS
        rotY.axis = Rotate.Y_AXIS
        rotZ.axis = Rotate.Z_AXIS
        transforms.addAll(translate, rotZ, rotY, rotX, scale)
    }
}
