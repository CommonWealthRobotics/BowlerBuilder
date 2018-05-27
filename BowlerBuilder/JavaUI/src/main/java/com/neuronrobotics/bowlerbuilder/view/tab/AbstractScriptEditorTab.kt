/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.tab

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView
import javafx.scene.Node

/**
 * Tab used for editing scripts (and running them).
 *
 * @param title the title of this tab
 * @param scriptEditorView the [ScriptEditorView], which contains the visual content and the actual
 * script editor
 * @param <T> controller type
 */
abstract class AbstractScriptEditorTab<T>(
    title: String,
    open val scriptEditorView: ScriptEditorView
) : AbstractTab<T>(title) {

    override val view: Node
        get() = scriptEditorView.getView()
}
