/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.tab

import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView

/**
 * A tab used for editing CAD scripts (and displaying the result).
 *
 * @param title the title of this tab
 * @param scriptEditorView the [ScriptEditorView], which contains the visual content and the actual
 * script editor
 * @param cadEngine the [CadEngine] used to display CAD objects
 * @param <T> controller type
 */
abstract class AbstractCadEditorTab<T>(
    title: String,
    scriptEditorView: ScriptEditorView,
    val cadEngine: CadEngine
) : AbstractScriptEditorTab<T>(title, scriptEditorView)
