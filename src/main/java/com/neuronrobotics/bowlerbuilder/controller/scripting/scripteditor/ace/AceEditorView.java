/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.controller.AceScriptEditorController;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.annotation.Nonnull;

/**
 * View to an {@link AceEditor} through a {@link WebView}.
 */
public class AceEditorView implements ScriptEditorView {

  private final WebView webView;
  private final AceEditor scriptEditor;

  @Inject
  public AceEditorView(@Nonnull final WebView webView,
      @Nonnull final AceWebEngineFactory aceWebEngineFactory) {
    this.webView = webView;

    final WebEngine webEngine = webView.getEngine();
    webView.getEngine().setJavaScriptEnabled(true);
    Platform.runLater(() -> webView.getEngine().load(AceScriptEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/web/ace.html").toString()));

    this.scriptEditor = new AceEditor(aceWebEngineFactory.create(webEngine));
  }

  @Override
  public Node getView() {
    return webView;
  }

  @Override
  public ScriptEditor getScriptEditor() {
    return scriptEditor;
  }

}
