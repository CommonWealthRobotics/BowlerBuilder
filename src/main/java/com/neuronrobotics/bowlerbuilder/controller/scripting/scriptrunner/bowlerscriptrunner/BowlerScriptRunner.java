package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.bowlerscriptrunner;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.annotation.Nonnull;

/**
 * {@link ScriptRunner} passthrough to {@link ScriptingEngine}.
 */
public class BowlerScriptRunner implements ScriptRunner {

  private final BowlerGroovy language;
  private final ObjectProperty<Object> result;

  @Inject
  public BowlerScriptRunner(@Nonnull final BowlerGroovy language) {
    this.language = language;
    result = new SimpleObjectProperty<>();
    ScriptingEngine.addScriptingLanguage(language);
  }

  @Override
  public Object runScript(@Nonnull final String script, final ArrayList<Object> arguments,
      @Nonnull final String languageName) throws Exception { //NOPMD
    result.set(ScriptingEngine.inlineScriptStringRun(script, arguments, languageName));
    return result.get();
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

  @Override
  public ReadOnlyObjectProperty<Object> resultProperty() {
    return result;
  }

}
