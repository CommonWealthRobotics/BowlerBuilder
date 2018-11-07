/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.tab.cadeditor

import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine.BowlerCadEngine
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace.AceEditorView

/**
 * A [BaseCadEditorTab] that uses an [AceEditorView] and a [BowlerCadEngine].
 *
 * @param title the title of this tab
 */
class AceCadEditorTab(title: String) : BaseCadEditorTab(
        title,
        BowlerBuilder.injector.getInstance(AceEditorView::class.java),
        BowlerBuilder.injector.getInstance(BowlerCadEngine::class.java))
