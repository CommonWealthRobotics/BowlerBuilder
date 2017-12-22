package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;

public class FileEditorControllerModule extends AbstractModule {

  private final ScriptEditorView scriptEditorView;

  public FileEditorControllerModule(ScriptEditorView scriptEditorView) {
    this.scriptEditorView = scriptEditorView;
  }

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).toInstance(scriptEditorView);
  }

}
