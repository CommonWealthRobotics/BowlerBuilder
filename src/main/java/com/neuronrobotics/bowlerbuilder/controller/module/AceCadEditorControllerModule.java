package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.BowlerScriptRunner;
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner;

public class AceCadEditorControllerModule extends AbstractModule {

  private final ScriptEditorView scriptEditorView;

  public AceCadEditorControllerModule(ScriptEditorView scriptEditorView) {
    this.scriptEditorView = scriptEditorView;
  }

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).toInstance(scriptEditorView);
    bind(ScriptRunner.class).to(BowlerScriptRunner.class);
    bind(String.class).annotatedWith(Names.named("scriptLangName")).toInstance("AwareGroovy");
  }

}
