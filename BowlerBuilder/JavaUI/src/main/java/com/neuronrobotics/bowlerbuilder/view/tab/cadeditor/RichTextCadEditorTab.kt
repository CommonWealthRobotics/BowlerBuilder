package com.neuronrobotics.bowlerbuilder.view.tab.cadeditor

import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine.BowlerCadEngine
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.richtext.RichTextEditorView

/**
 * A [BaseCadEditorTab] that uses a [RichTextEditorView] and a [BowlerCadEngine].
 *
 * @param title the title of this tab
 */
class RichTextCadEditorTab(title: String) : BaseCadEditorTab(
        title,
        BowlerBuilder.getInjector().getInstance(RichTextEditorView::class.java),
        BowlerBuilder.getInjector().getInstance(BowlerCadEngine::class.java))
