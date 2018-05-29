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
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerScriptRunner;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DefaultCadEditorControllerModule extends AbstractModule {

  private final ScriptEditorView scriptEditorView;

  public DefaultCadEditorControllerModule(final ScriptEditorView scriptEditorView) {
    super();
    this.scriptEditorView = scriptEditorView;
  }

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).toInstance(scriptEditorView);
    bind(ScriptRunner.class).to(BowlerScriptRunner.class);
    bind(String.class)
        .annotatedWith(Names.named("defaultScriptEditorLangName"))
        .toInstance("BowlerGroovy");
  }
}