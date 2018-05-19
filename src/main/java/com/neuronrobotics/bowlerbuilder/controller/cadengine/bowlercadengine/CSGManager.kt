/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine

import com.neuronrobotics.bowlerbuilder.controller.cadengine.util.CsgParser
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.shape.MeshView
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class CSGManager @Inject constructor(val csgParser: CsgParser) {
    val csgMap: Map<CSG, MeshView>
    val csgNameMap: Map<String, MeshView>

    init {
        csgMap = ConcurrentHashMap()
        csgNameMap = ConcurrentHashMap()
    }
}
