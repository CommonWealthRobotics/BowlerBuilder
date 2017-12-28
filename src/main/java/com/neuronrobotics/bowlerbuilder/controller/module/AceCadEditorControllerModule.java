package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner.BowlerScriptRunner;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesServiceFactory;

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
