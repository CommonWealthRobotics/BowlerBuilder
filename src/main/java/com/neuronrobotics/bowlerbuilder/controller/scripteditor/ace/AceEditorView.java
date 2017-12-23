package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.controller.AceCadEditorController;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * View to an {@link AceEditor} through a {@link WebView}.
 */
public class AceEditorView implements ScriptEditorView {

  private final WebView webView;
  private final AceEditor scriptEditor;

  @Inject
  public AceEditorView(WebView webView) {
    this.webView = webView;

    WebEngine webEngine = webView.getEngine();
    webView.getEngine().setJavaScriptEnabled(true);
    Platform.runLater(() -> webView.getEngine().load(AceCadEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/web/ace.html").toString()));

    this.scriptEditor = new AceEditor(new AceWebEngine(webEngine));
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
