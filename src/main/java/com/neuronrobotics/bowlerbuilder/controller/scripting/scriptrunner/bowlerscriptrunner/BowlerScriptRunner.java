package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.bowlerscriptrunner;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * {@link ScriptRunner} passthrough to {@link ScriptingEngine}.
 */
public class BowlerScriptRunner implements ScriptRunner {

  private final BowlerGroovy language;

  @Inject
  public BowlerScriptRunner(BowlerGroovy language) {
    this.language = language;
    ScriptingEngine.addScriptingLanguage(language);
  }

  @Override
  public Object runScript(String script, ArrayList<Object> arguments, String languageName)
      throws Exception {
    return ScriptingEngine.inlineScriptStringRun(script, arguments, languageName);
  }

  @Override
  public boolean isScriptCompiling() {
    return language.compilingProperty().getValue();
  }

  @Override
  public ReadOnlyBooleanProperty scriptCompilingProperty() {
    return language.compilingProperty();
  }

  @Override
  public boolean isScriptRunning() {
    return language.runningProperty().getValue();
  }

  @Override
  public ReadOnlyBooleanProperty scriptRunningProperty() {
    return language.runningProperty();
  }

}
