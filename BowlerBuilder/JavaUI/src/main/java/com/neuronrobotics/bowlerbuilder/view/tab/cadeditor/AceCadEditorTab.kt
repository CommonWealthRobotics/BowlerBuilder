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
        BowlerBuilder.getInjector().getInstance(AceEditorView::class.java),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine::class.java))
