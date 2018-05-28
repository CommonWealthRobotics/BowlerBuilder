package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.richtextfx;

import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;

public final class RichTextEditor implements ScriptEditor {

  private final CodeArea codeArea;

  public RichTextEditor(final CodeArea codeArea) {
    this.codeArea = codeArea;
  }

  @Override
  public void insertAtCursor(@NotNull String text) {
    codeArea.insertText(codeArea.getCaretPosition(), text);
  }

  @Override
  public void setText(@NotNull String text) {
    codeArea.replaceText(0, codeArea.getLength(), text);
  }

  @NotNull
  @Override
  public String getFullText() {
    return codeArea.getText();
  }

  @NotNull
  @Override
  public String getSelectedText() {
    return codeArea.getSelectedText();
  }

  @Override
  public void gotoLine(int lineNumber) {}

  @Override
  public int getCursorPosition() {
    return 0;
  }
}
