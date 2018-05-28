/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace

import com.neuronrobotics.bowlerbuilder.controller.DefaultScriptEditorController
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditor
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.view.util.WebEngineUtil.runAfterEngine
import javafx.scene.Node
import javafx.scene.web.WebView
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
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
            webView.engine.load(DefaultScriptEditorController::class.java
                    .getResource("/com/neuronrobotics/bowlerbuilder/web/ace.html")
                    .toString())
        }
    }

    override fun setFontSize(fontSize: Int) {
        runBlocking {
            launch(context = UI) {
                runAfterEngine(webView.engine.loadWorker, Runnable {
                    webView.engine.executeScript("document.getElementById('editor').style" +
                            ".fontSize='${fontSize}px';")
                })
            }
        }
    }

    override fun getView(): Node = webView

    override fun getScriptEditor(): ScriptEditor = scriptEditor
}
