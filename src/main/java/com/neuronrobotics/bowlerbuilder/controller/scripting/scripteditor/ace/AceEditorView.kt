/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace

import com.neuronrobotics.bowlerbuilder.controller.AceScriptEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView
import javafx.scene.Node
import javafx.scene.web.WebView
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject
import kotlinx.coroutines.experimental.javafx.JavaFx as UI

/**
 * A view to an [AceEditor] through a [WebView].
 */
class AceEditorView @Inject constructor(
    private val webView: WebView,
    aceWebEngineFactory: AceWebEngineFactory
) : ScriptEditorView {

    private val scriptEditor: AceEditor

    init {
        webView.engine.isJavaScriptEnabled = true
        scriptEditor = AceEditor(aceWebEngineFactory.create(webView.engine))

        launch(context = UI) {
            webView.engine.load(AceScriptEditorController::class.java
                    .getResource("/com/neuronrobotics/bowlerbuilder/web/ace.html")
                    .toString())
        }
    }

    override fun getView(): Node =
            webView

    override fun getScriptEditor(): ScriptEditor =
            scriptEditor
}
