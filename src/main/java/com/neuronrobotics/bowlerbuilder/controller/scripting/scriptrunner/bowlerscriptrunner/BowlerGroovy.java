package com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.bowlerscriptrunner;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.annotation.Nonnull;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Simple copy of {@link GroovyHelper} that keeps a flag for when it is compiling or running.
 */
public class BowlerGroovy implements IScriptingLanguage {

  private static final Logger LOGGER
      = LoggerUtilities.getLogger(BowlerGroovy.class.getSimpleName());
  private final BooleanProperty compiling;
  private final BooleanProperty running;

  public BowlerGroovy() {
    compiling = new SimpleBooleanProperty(false);
    running = new SimpleBooleanProperty(false);
  }

  @Override
  public String getShellType() {
    return "BowlerGroovy";
  }

  @Override
  public Object inlineScriptRun(@Nonnull final File code, @Nonnull final ArrayList<Object> args)
      throws Exception { //NOPMD
    return this.inline(code, args);
  }

  @Override
  public Object inlineScriptRun(@Nonnull final String code, @Nonnull final ArrayList<Object> args)
      throws Exception { //NOPMD
    return this.inline(code, args);
  }

  @Override
  public boolean getIsTextFile() {
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    return new ArrayList<>(Arrays.asList("java", "groovy"));
  }

  private Object inline(@Nonnull final Object code,
      @Nonnull final List<Object> args) throws Exception { //NOPMD
    compiling.setValue(true);

    final CompilerConfiguration configuration = new CompilerConfiguration();
    configuration.addCompilationCustomizers(new ImportCustomizer()
        .addStarImports(ScriptingEngine.getImports())
        .addStarImports(
            "com.neuronrobotics.bowlerbuilder",
            "com.neuronrobotics.bowlerbuilder.controller",
            "com.neuronrobotics.bowlerbuilder.view.tab")
        .addStaticStars(
            "com.neuronrobotics.sdk.util.ThreadUtil",
            "eu.mihosoft.vrl.v3d.Transform",
            "com.neuronrobotics.bowlerstudio.vitamins.Vitamins"));

    final Binding binding = new Binding();

    for (final String pm : DeviceManager.listConnectedDevice()) {
      final BowlerAbstractDevice device = DeviceManager.getSpecificDevice(null, pm);

      binding.setVariable(
          device.getScriptingName(),
          Class.forName(device.getClass().getName()).cast(device));
    }

    binding.setVariable("args", args);

    final GroovyShell shell = new GroovyShell(GroovyHelper.class.getClassLoader(), binding,
        configuration);
    final Script script;

    if (String.class.isInstance(code)) {
      script = shell.parse((String) code);
    } else {
      if (!File.class.isInstance(code)) {
        return null;
      }

      script = shell.parse((File) code);
    }

    compiling.setValue(false);

    running.setValue(true);
    final Object result = script.run();
    running.setValue(false);

    return result;
  }

  public ReadOnlyBooleanProperty compilingProperty() {
    return compiling;
  }

  public ReadOnlyBooleanProperty runningProperty() {
    return running;
  }

}
