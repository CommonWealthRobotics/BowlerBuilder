package com.neuronrobotics.bowlerbuilder.controller.scripteditor.groovy;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.groovy.ast.GroovyTreeTransformation;
import com.neuronrobotics.bowlerbuilder.model.tree.AST;
import com.neuronrobotics.bowlerbuilder.model.tree.TreeNode;
import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.IDebugScriptRunner;
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
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Simple copy of {@link GroovyHelper} that keeps a flag for when it is compiling or running.
 */
public class AwareGroovyLanguage implements IScriptingLanguage {

  private static final Logger logger
      = LoggerUtilities.getLogger(AwareGroovyLanguage.class.getSimpleName());
  private final BooleanProperty compilingProperty;
  private final BooleanProperty runningProperty;

  public AwareGroovyLanguage() {
    compilingProperty = new SimpleBooleanProperty(false);
    runningProperty = new SimpleBooleanProperty(false);
  }

  @Override
  public String getShellType() {
    return "AwareGroovy";
  }

  @Override
  public Object inlineScriptRun(File code, ArrayList<Object> args) throws Exception {
    return this.inline(code, args);
  }

  @Override
  public Object inlineScriptRun(String code, ArrayList<Object> args) throws Exception {
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

  private Object inline(Object code, ArrayList<Object> args) throws Exception {
    compilingProperty.setValue(true);

    CompilerConfiguration cc = new CompilerConfiguration();
    cc.addCompilationCustomizers(new ImportCustomizer()
        .addStarImports(ScriptingEngine.getImports())
        .addStaticStars(
            "com.neuronrobotics.sdk.util.ThreadUtil",
            "eu.mihosoft.vrl.v3d.Transform",
            "com.neuronrobotics.bowlerstudio.vitamins.Vitamins"));
    GroovyTreeTransformation transformation = new GroovyTreeTransformation();
    cc.addCompilationCustomizers(new ASTTransformationCustomizer(transformation));

    Binding binding = new Binding();

    for (String pm : DeviceManager.listConnectedDevice()) {
      BowlerAbstractDevice device = DeviceManager.getSpecificDevice(null, pm);

      binding.setVariable(
          device.getScriptingName(),
          Class.forName(device.getClass().getName()).cast(device));
    }

    binding.setVariable("args", args);

    GroovyShell shell = new GroovyShell(GroovyHelper.class.getClassLoader(), binding, cc);
    Script script;

    if (String.class.isInstance(code)) {
      script = shell.parse((String) code);
    } else {
      if (!File.class.isInstance(code)) {
        return null;
      }

      script = shell.parse((File) code);
    }

    compilingProperty.setValue(false);

    runningProperty.setValue(true);
    Object result = script.run();
    runningProperty.setValue(false);

    AST ast = new AST(transformation.getTree());
    printTree(ast.getRoot(), "");

    return result;
  }

  /**
   * Helper to print a tree for debugging.
   *
   * @param node root node
   * @param prefix starting prefix
   */
  private void printTree(TreeNode<String> node, String prefix) {
    System.out.println(prefix + node.getData()); //NOPMD
    node.getChildren().forEach(child -> printTree(child, prefix + "\t"));
  }

  public ReadOnlyBooleanProperty compilingProperty() {
    return compilingProperty;
  }

  public ReadOnlyBooleanProperty runningProperty() {
    return runningProperty;
  }

  public IDebugScriptRunner compileDebug(File file) {
    return () -> new String[]{"fileame.groovy", "345"};
  }

}
