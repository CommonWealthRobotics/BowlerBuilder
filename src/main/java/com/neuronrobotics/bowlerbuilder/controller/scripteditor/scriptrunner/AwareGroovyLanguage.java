package com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.model.tree.AST;
import com.neuronrobotics.bowlerbuilder.model.tree.ASTNode;
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
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

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
    TreeTransformation transformation = new TreeTransformation();
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

  private void printTree(TreeNode<String> node, String prefix) {
    System.out.println(prefix + node.getData());
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

  @GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
  private class TreeTransformation implements ASTTransformation {

    private TreeVisitor visitor = new TreeVisitor();

    public ASTNode getTree() {
      return visitor.getRoot();
    }

    @Override
    public void visit(org.codehaus.groovy.ast.ASTNode[] astNodes, SourceUnit sourceUnit) {
      sourceUnit.getAST().getClasses().forEach(klass -> {
        ASTNode classNode = new ASTNode("Class: " + klass.getName());
        klass.getMethods().forEach(method -> {
          ASTNode methodNode = new ASTNode("Method: " + method.getName());
          TreeVisitor temp = new TreeVisitor();
          method.getCode().visit(temp);
          methodNode.addChild(temp.getRoot());
          classNode.addChild(methodNode);
        });
        visitor.getRoot().addChild(classNode);
      });
    }

    private class TreeVisitor implements GroovyCodeVisitor {

      private ASTNode root = new ASTNode("root");

      public ASTNode getRoot() {
        return root;
      }

      @Override
      public void visitBlockStatement(BlockStatement blockStatement) {
        ASTNode node = new ASTNode("<block>");
        blockStatement.getStatements().forEach(statement -> {
          TreeVisitor visitor = new TreeVisitor();
          statement.visit(visitor);
          node.addChild(visitor.getRoot());
        });
        root.addChild(node);
      }

      @Override
      public void visitForLoop(ForStatement forStatement) {
        ASTNode node = new ASTNode("<for>");

        TreeVisitor visitor = new TreeVisitor();
        forStatement.getCollectionExpression().visit(visitor);
        node.addChild(visitor.getRoot());

        TreeVisitor loopVisitor = new TreeVisitor();
        forStatement.getLoopBlock().visit(loopVisitor);
        node.addChild(loopVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitWhileLoop(WhileStatement whileStatement) {
        ASTNode node = new ASTNode("<while>");
        node.addChild(whileStatement.getLoopBlock().getStatementLabel());
        root.addChild(node);
      }

      @Override
      public void visitDoWhileLoop(DoWhileStatement doWhileStatement) {
        ASTNode node = new ASTNode("<do while>");
        node.addChild(doWhileStatement.getLoopBlock().getStatementLabel());
        root.addChild(node);
      }

      @Override
      public void visitIfElse(IfStatement ifStatement) {
        ASTNode node = new ASTNode("<if statement>");
        node.addChild(ifStatement.getBooleanExpression().getText());

        TreeVisitor ifVisitor = new TreeVisitor();
        ifStatement.getIfBlock().visit(ifVisitor);
        node.addChild(ifVisitor.getRoot());

        TreeVisitor elseVisitor = new TreeVisitor();
        ifStatement.getElseBlock().visit(elseVisitor);
        node.addChild(elseVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitExpressionStatement(ExpressionStatement expressionStatement) {
        ASTNode node = new ASTNode("<expression>");
        TreeVisitor visitor = new TreeVisitor();
        expressionStatement.getExpression().visit(visitor);
        node.addChild(visitor.getRoot());
        root.addChild(node);
      }

      @Override
      public void visitReturnStatement(ReturnStatement returnStatement) {
        root.addChild(returnStatement.getText());
      }

      @Override
      public void visitAssertStatement(AssertStatement assertStatement) {
        ASTNode node = new ASTNode("<assert>");
        node.addChild(assertStatement.getBooleanExpression().getText());
        node.addChild(assertStatement.getMessageExpression().getText());
        root.addChild(node);
      }

      @Override
      public void visitTryCatchFinally(TryCatchStatement tryCatchStatement) {
        ASTNode node = new ASTNode("<try catch>");

        TreeVisitor tryVisitor = new TreeVisitor();
        tryCatchStatement.getTryStatement().visit(tryVisitor);
        node.addChild(tryVisitor.getRoot());

        ASTNode catchNodes = new ASTNode("<catches>");
        tryCatchStatement.getCatchStatements().forEach(catchStatement -> {
          TreeVisitor catchVisitor = new TreeVisitor();
          catchStatement.getCode().visit(catchVisitor);
          catchNodes.addChild(catchVisitor.getRoot());
        });
        node.addChild(catchNodes);

        TreeVisitor finallyVisitor = new TreeVisitor();
        tryCatchStatement.getFinallyStatement().visit(finallyVisitor);
        node.addChild(finallyVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitSwitch(SwitchStatement switchStatement) {
        ASTNode node = new ASTNode("<switch>");

        TreeVisitor visitor = new TreeVisitor();
        switchStatement.getExpression().visit(visitor);
        node.addChild(visitor.getRoot());

        ASTNode caseNodes = new ASTNode("<cases>");
        switchStatement.getCaseStatements().forEach(caseStatement -> {
          TreeVisitor caseVisitor = new TreeVisitor();
          caseStatement.getCode().visit(caseVisitor);
          caseNodes.addChild(caseVisitor.getRoot());
        });
        node.addChild(caseNodes);

        TreeVisitor defaultVisitor = new TreeVisitor();
        switchStatement.getDefaultStatement().visit(defaultVisitor);
        root.addChild(defaultVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitCaseStatement(CaseStatement caseStatement) {
        TreeVisitor visitor = new TreeVisitor();
        caseStatement.getCode().visit(visitor);
        root.addChild(visitor.getRoot());
      }

      @Override
      public void visitBreakStatement(BreakStatement breakStatement) {
        root.addChild("<break>");
      }

      @Override
      public void visitContinueStatement(ContinueStatement continueStatement) {
        root.addChild("<continue>");
      }

      @Override
      public void visitThrowStatement(ThrowStatement throwStatement) {
        TreeVisitor visitor = new TreeVisitor();
        throwStatement.getExpression().visit(visitor);
        root.addChild(visitor.getRoot());
      }

      @Override
      public void visitSynchronizedStatement(SynchronizedStatement synchronizedStatement) {
        ASTNode node = new ASTNode("<synchronized>");

        TreeVisitor exVisitor = new TreeVisitor();
        synchronizedStatement.getExpression().visit(exVisitor);
        node.addChild(exVisitor.getRoot());

        TreeVisitor stmtVisitor = new TreeVisitor();
        synchronizedStatement.getCode().visit(stmtVisitor);
        node.addChild(stmtVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitCatchStatement(CatchStatement catchStatement) {
        TreeVisitor visitor = new TreeVisitor();
        catchStatement.getCode().visit(visitor);
        root.addChild(visitor.getRoot());
      }

      @Override
      public void visitMethodCallExpression(MethodCallExpression methodCallExpression) {
        root.addChild(methodCallExpression.getText());
      }

      @Override
      public void visitStaticMethodCallExpression(StaticMethodCallExpression
                                                      staticMethodCallExpression) {
        root.addChild(staticMethodCallExpression.getText());
      }

      @Override
      public void visitConstructorCallExpression(ConstructorCallExpression
                                                     constructorCallExpression) {
        root.addChild(constructorCallExpression.getText());
      }

      @Override
      public void visitTernaryExpression(TernaryExpression ternaryExpression) {
        root.addChild(ternaryExpression.getText());
      }

      @Override
      public void visitShortTernaryExpression(ElvisOperatorExpression elvisOperatorExpression) {
        root.addChild("<elvis>");
      }

      @Override
      public void visitBinaryExpression(BinaryExpression binaryExpression) {
        ASTNode node = new ASTNode("<binary expression>");

        TreeVisitor leftVisitor = new TreeVisitor();
        binaryExpression.getLeftExpression().visit(leftVisitor);
        node.addChild(leftVisitor.getRoot());

        node.addChild(binaryExpression.getOperation().getText());

        TreeVisitor rightVisitor = new TreeVisitor();
        binaryExpression.getRightExpression().visit(rightVisitor);
        node.addChild(rightVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitPrefixExpression(PrefixExpression prefixExpression) {
        root.addChild(prefixExpression.getText());
      }

      @Override
      public void visitPostfixExpression(PostfixExpression postfixExpression) {
        root.addChild(postfixExpression.getText());
      }

      @Override
      public void visitBooleanExpression(BooleanExpression booleanExpression) {
        root.addChild(booleanExpression.getText());
      }

      @Override
      public void visitClosureExpression(ClosureExpression closureExpression) {
        root.addChild(closureExpression.getText());
      }

      @Override
      public void visitTupleExpression(TupleExpression tupleExpression) {
        root.addChild(tupleExpression.getText());
      }

      @Override
      public void visitMapExpression(MapExpression mapExpression) {
        root.addChild(mapExpression.getText());
      }

      @Override
      public void visitMapEntryExpression(MapEntryExpression mapEntryExpression) {
        ASTNode node = new ASTNode("<map entry>");

        TreeVisitor keyVisitor = new TreeVisitor();
        mapEntryExpression.getKeyExpression().visit(keyVisitor);
        node.addChild(keyVisitor.getRoot());

        TreeVisitor valueVisitor = new TreeVisitor();
        mapEntryExpression.getValueExpression().visit(valueVisitor);
        node.addChild(valueVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitListExpression(ListExpression listExpression) {
        root.addChild(listExpression.getText());
      }

      @Override
      public void visitRangeExpression(RangeExpression rangeExpression) {
        root.addChild(rangeExpression.getText());
      }

      @Override
      public void visitPropertyExpression(PropertyExpression propertyExpression) {
        root.addChild(propertyExpression.getText());
      }

      @Override
      public void visitAttributeExpression(AttributeExpression attributeExpression) {
        root.addChild("<attribute>");
      }

      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        root.addChild(fieldExpression.toString());
      }

      @Override
      public void visitMethodPointerExpression(MethodPointerExpression methodPointerExpression) {
        root.addChild(methodPointerExpression.getText());
      }

      @Override
      public void visitConstantExpression(ConstantExpression constantExpression) {
        root.addChild(constantExpression.toString());
      }

      @Override
      public void visitClassExpression(ClassExpression classExpression) {
        root.addChild(classExpression.getText());
      }

      @Override
      public void visitVariableExpression(VariableExpression variableExpression) {
        ASTNode node = new ASTNode("<variable>");

        ASTNode typeNode = new ASTNode("<type>");
        typeNode.addChild(variableExpression.getType().getName());
        node.addChild(typeNode);

        ASTNode nameNode = new ASTNode("<name>");
        nameNode.addChild(variableExpression.getText());
        node.addChild(nameNode);

        root.addChild(node);
      }

      @Override
      public void visitDeclarationExpression(DeclarationExpression declarationExpression) {
        ASTNode node = new ASTNode("<declaration>");

        TreeVisitor visitor = new TreeVisitor();
        declarationExpression.getVariableExpression().visit(visitor);
        node.addChild(visitor.getRoot());

        TreeVisitor rightVisitor = new TreeVisitor();
        declarationExpression.getRightExpression().visit(rightVisitor);
        node.addChild(rightVisitor.getRoot());

        root.addChild(node);
      }

      @Override
      public void visitGStringExpression(GStringExpression gStringExpression) {
        root.addChild(gStringExpression.getText());
      }

      @Override
      public void visitArrayExpression(ArrayExpression arrayExpression) {
        root.addChild(arrayExpression.getText());
      }

      @Override
      public void visitSpreadExpression(SpreadExpression spreadExpression) {
        root.addChild(spreadExpression.getText());
      }

      @Override
      public void visitSpreadMapExpression(SpreadMapExpression spreadMapExpression) {
        root.addChild(spreadMapExpression.getText());
      }

      @Override
      public void visitNotExpression(NotExpression notExpression) {
        root.addChild("<not>");
      }

      @Override
      public void visitUnaryMinusExpression(UnaryMinusExpression unaryMinusExpression) {
        root.addChild(unaryMinusExpression.getText());
      }

      @Override
      public void visitUnaryPlusExpression(UnaryPlusExpression unaryPlusExpression) {
        root.addChild(unaryPlusExpression.getText());
      }

      @Override
      public void visitBitwiseNegationExpression(BitwiseNegationExpression
                                                     bitwiseNegationExpression) {
        root.addChild(bitwiseNegationExpression.getText());
      }

      @Override
      public void visitCastExpression(CastExpression castExpression) {
        root.addChild(castExpression.getText());
      }

      @Override
      public void visitArgumentlistExpression(ArgumentListExpression argumentListExpression) {
        root.addChild("<argument list>");
      }

      @Override
      public void visitClosureListExpression(ClosureListExpression closureListExpression) {
        root.addChild(closureListExpression.getText());
      }

      @Override
      public void visitBytecodeExpression(BytecodeExpression bytecodeExpression) {
        root.addChild("<bytecode>");
      }

    }

  }

}
