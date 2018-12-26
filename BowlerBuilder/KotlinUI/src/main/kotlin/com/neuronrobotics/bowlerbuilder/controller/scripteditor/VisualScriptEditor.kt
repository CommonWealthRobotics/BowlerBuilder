/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import javafx.scene.Parent

/**
 * A [ScriptEditor] which can also be interfaces through visually using the [root] node.
 */
interface VisualScriptEditor : ScriptEditor {
    val root: Parent
}
