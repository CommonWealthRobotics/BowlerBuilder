/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller

import eu.mihosoft.vrl.v3d.CSG

class AceCadEditorTabController(
    val aceScriptEditorController: AceScriptEditorController,
    val cadModelViewerController: CADModelViewerController
) {

    private fun parseCSG(item: Any?) {
        if (item is CSG) {
            cadModelViewerController.addCSG(item)
        } else if (item is Iterable<*>) {
            item.forEach { parseCSG(it) }
        }
    }

    init {
        aceScriptEditorController
                .scriptRunner
                .resultProperty()
                .addListener({ _, _, newValue ->
                    cadModelViewerController.clearMeshes()
                    parseCSG(newValue) })
    }
}
