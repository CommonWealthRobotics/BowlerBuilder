package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.richtext;

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.richtextfx.RichTextEditor;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor;
import javafx.scene.Node;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;

public class RichTextEditorView implements ScriptEditorView {

  private CodeArea codeArea;
  private RichTextEditor editor;

  public RichTextEditorView() {
    codeArea = new CodeArea();
    editor = new RichTextEditor(codeArea);
  }

  @Override
  public void setFontSize(int fontSize) {}

  @NotNull
  @Override
  public Node getView() {
    return new VirtualizedScrollPane<>(codeArea);
  }

  @NotNull
  @Override
  public ScriptEditor getScriptEditor() {
    return editor;
  }
}
