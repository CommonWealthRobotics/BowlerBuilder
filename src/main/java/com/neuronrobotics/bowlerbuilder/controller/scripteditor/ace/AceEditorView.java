package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace;

import com.neuronrobotics.bowlerbuilder.controller.FileEditorController;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * View to an {@link AceEditor} through a {@link WebView}.
 */
public class AceEditorView implements ScriptEditorView {

  private final WebView webView;
  private final AceEditor scriptEditor;

  public AceEditorView() {
    webView = new WebView();
    this.scriptEditor = new AceEditor(webView.getEngine());

    webView.getEngine().setJavaScriptEnabled(true);
    Platform.runLater(() -> webView.getEngine().load(FileEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/web/ace.html").toString()));
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
