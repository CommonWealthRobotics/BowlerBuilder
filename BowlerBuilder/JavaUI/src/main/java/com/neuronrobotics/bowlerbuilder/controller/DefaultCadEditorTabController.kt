/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import eu.mihosoft.vrl.v3d.CSG

class DefaultCadEditorTabController(
    val defaultScriptEditorController: DefaultScriptEditorController,
    val defaultCadModelViewerController: DefaultCADModelViewerController
) {

    private fun parseCSG(item: Any?) {
        if (item is CSG) {
            defaultCadModelViewerController.addCSG(item)
        } else if (item is Iterable<*>) {
            item.forEach { parseCSG(it) }
        }
    }

    init {
        defaultScriptEditorController
                .scriptRunner
                .resultProperty()
                .addListener { _, _, newValue ->
                    defaultCadModelViewerController.clearMeshes()
                    parseCSG(newValue.getSuccess())
                }
    }
}
