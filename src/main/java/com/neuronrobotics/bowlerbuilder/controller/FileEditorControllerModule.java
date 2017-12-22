package com.neuronrobotics.bowlerbuilder.controller;

import com.google.inject.AbstractModule;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;

public class FileEditorControllerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).to(AceEditorView.class);
  }

}
