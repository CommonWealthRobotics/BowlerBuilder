package com.neuronrobotics.bowlerbuilder.controller.scripteditor;

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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

public class TestGroovy implements IScriptingLanguage {

  private Object inline(Object code, ArrayList<Object> args) throws Exception {
    CompilerConfiguration cc = new CompilerConfiguration();
    cc.addCompilationCustomizers((new ImportCustomizer())
        .addStarImports(ScriptingEngine.getImports())
        .addStaticStars(
            "com.neuronrobotics.sdk.util.ThreadUtil",
            "eu.mihosoft.vrl.v3d.Transform",
            "com.neuronrobotics.bowlerstudio.vitamins.Vitamins"));
    cc.addCompilationCustomizers(new ASTTransformationCustomizer(new ExploreAST()));

    Binding binding = new Binding();

    for (String pm : DeviceManager.listConnectedDevice()) {
      BowlerAbstractDevice bad = DeviceManager.getSpecificDevice(null, pm);

      binding.setVariable(
          bad.getScriptingName(),
          Class.forName(bad.getClass().getName()).cast(bad));
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

    return script.run();
  }

  @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
  class ExploreAST implements ASTTransformation {

    @Override
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
      System.out.println("Source Name: " + sourceUnit.getName());
      sourceUnit.getAST().getClasses().forEach(this::explore);
    }

    private void explore(ClassNode classNode) {
      System.out.println("\tClass Name: " + classNode.getName());
      classNode.getMethods().forEach(this::explore);
    }

    private void explore(MethodNode methodNode) {
      System.out.println("\t\tMethod Name: " + methodNode.getName());
    }
  }

  @Override
  public String getShellType() {
    return "TestGroovy";
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

}
