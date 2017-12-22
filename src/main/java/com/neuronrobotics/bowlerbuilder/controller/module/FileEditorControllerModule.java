package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace.AceEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;

public class FileEditorControllerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).to(AceEditorView.class);
  }

}
