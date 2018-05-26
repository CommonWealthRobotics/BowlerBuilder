/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.bowlerscriptrunner.BowlerScriptRunner;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AceCadEditorControllerModule extends AbstractModule {

  private final ScriptEditorView scriptEditorView;

  public AceCadEditorControllerModule(final ScriptEditorView scriptEditorView) {
    super();
    this.scriptEditorView = scriptEditorView;
  }

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).toInstance(scriptEditorView);
    bind(ScriptRunner.class).to(BowlerScriptRunner.class);
    bind(String.class).annotatedWith(Names.named("scriptLangName")).toInstance("BowlerGroovy");
  }
}
