/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
