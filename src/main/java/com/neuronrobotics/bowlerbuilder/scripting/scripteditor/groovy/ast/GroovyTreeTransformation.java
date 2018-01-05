package com.neuronrobotics.bowlerbuilder.scripting.scripteditor.groovy.ast; //NOPMD

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ArgumentListNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ArrayNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.AssertNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.AttributeNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.BinaryNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.BitwiseNegationNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.BlockNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.BooleanNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.BreakNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.BytecodeNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.CaseNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.CastNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.CatchNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ClassDeclarationNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ClassExpressionNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ClosureListNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ClosureNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ConstantNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ConstructorCallNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ContinueNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.DeclarationNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.DoWhileNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ExpressionNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.FieldNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ForNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.GStringNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.IfElseNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ListNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.MapEntryNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.MapNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.MethodCallNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.MethodNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.MethodPointerNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.NotNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.PostfixNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.PrefixNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.PropertyNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.RangeNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ReturnNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ShortTernaryNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.SpreadMapNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.SpreadNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.StaticMethodCallNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.SwitchNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.SynchronizedNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.TernaryNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.ThrowNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.TryCatchFinallyNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.TupleNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.UnaryMinusNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.UnaryPlusNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.VariableNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node.WhileNode;
import java.util.LinkedList;
import java.util.List;
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
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * {@link ASTTransformation} to build an KTree.
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class GroovyTreeTransformation implements ASTTransformation {

  private final TreeVisitor visitor = new TreeVisitor();

  public ASTNode getTree() {
    return visitor.getRoot();
  }

  @Override
  public void visit(org.codehaus.groovy.ast.ASTNode[] astNodes, SourceUnit sourceUnit) {
    sourceUnit.getAST().getClasses().forEach(klass -> {
      ClassDeclarationNode classNode = new ClassDeclarationNode(
          klass.getName(),
          klass.getLineNumber());

      klass.getMethods().forEach(method -> {
        MethodNode methodNode = new MethodNode(method.getName(), method.getLineNumber());

        TreeVisitor temp = new TreeVisitor();
        method.getCode().visit(temp);
        methodNode.addChild(temp.getRoot());

        classNode.addChild(methodNode);
      });

      visitor.getRoot().addChild(classNode);
    });
  }

  /**
   * {@link GroovyCodeVisitor} to add a statement to the internal tree. Used by
   * {@link GroovyTreeTransformation} to build a full KTree.
   */
  private class TreeVisitor implements GroovyCodeVisitor {

    private final ASTNode root = new ASTNode("root", ASTNodeType.RootNode);

    public ASTNode getRoot() {
      return root;
    }

    @Override
    public void visitBlockStatement(BlockStatement blockStatement) {
      List<ASTNode> children = new LinkedList<>();

      blockStatement.getStatements().forEach(statement -> {
        TreeVisitor visitor = new TreeVisitor();
        statement.visit(visitor);
        children.add(visitor.getRoot());
      });

      root.addChild(new BlockNode(children));
    }

    @Override
    public void visitForLoop(ForStatement forStatement) {
      TreeVisitor collectionVisitor = new TreeVisitor();
      forStatement.getCollectionExpression().visit(collectionVisitor);

      TreeVisitor loopVisitor = new TreeVisitor();
      forStatement.getLoopBlock().visit(loopVisitor);

      root.addChild(new ForNode(collectionVisitor.getRoot(), loopVisitor.getRoot()));
    }

    @Override
    public void visitWhileLoop(WhileStatement whileStatement) {
      root.addChild(new WhileNode(whileStatement.getLoopBlock().getStatementLabel()));
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement doWhileStatement) {
      root.addChild(new DoWhileNode(doWhileStatement.getLoopBlock().getStatementLabel()));
    }

    @Override
    public void visitIfElse(IfStatement ifStatement) {
      TreeVisitor ifVisitor = new TreeVisitor();
      ifStatement.getIfBlock().visit(ifVisitor);

      TreeVisitor elseVisitor = new TreeVisitor();
      ifStatement.getElseBlock().visit(elseVisitor);

      root.addChild(new IfElseNode(
          ifStatement.getBooleanExpression().getText(),
          ifVisitor.getRoot(),
          elseVisitor.getRoot()));
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement expressionStatement) {
      TreeVisitor visitor = new TreeVisitor();
      expressionStatement.getExpression().visit(visitor);
      root.addChild(new ExpressionNode(visitor.getRoot()));
    }

    @Override
    public void visitReturnStatement(ReturnStatement returnStatement) {
      root.addChild(new ReturnNode(returnStatement.getText()));
    }

    @Override
    public void visitAssertStatement(AssertStatement assertStatement) {
      root.addChild(new AssertNode(
          assertStatement.getBooleanExpression().getText(),
          assertStatement.getMessageExpression().getText()));
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement tryCatchStatement) {
      TreeVisitor tryVisitor = new TreeVisitor();
      tryCatchStatement.getTryStatement().visit(tryVisitor);

      List<ASTNode> catchNodes = new LinkedList<>();
      tryCatchStatement.getCatchStatements().forEach(catchStatement -> {
        TreeVisitor catchVisitor = new TreeVisitor();
        catchStatement.getCode().visit(catchVisitor);
        catchNodes.add(catchVisitor.getRoot());
      });

      TreeVisitor finallyVisitor = new TreeVisitor();
      tryCatchStatement.getFinallyStatement().visit(finallyVisitor);

      root.addChild(new TryCatchFinallyNode(
          tryVisitor.getRoot(),
          catchNodes,
          finallyVisitor.getRoot()
      ));
    }

    @Override
    public void visitSwitch(SwitchStatement switchStatement) {
      TreeVisitor visitor = new TreeVisitor();
      switchStatement.getExpression().visit(visitor);

      List<ASTNode> caseNodes = new LinkedList<>();
      switchStatement.getCaseStatements().forEach(caseStatement -> {
        TreeVisitor caseVisitor = new TreeVisitor();
        caseStatement.getCode().visit(caseVisitor);
        caseNodes.add(caseVisitor.getRoot());
      });

      TreeVisitor defaultVisitor = new TreeVisitor();
      switchStatement.getDefaultStatement().visit(defaultVisitor);

      root.addChild(new SwitchNode(
          visitor.getRoot(),
          caseNodes,
          defaultVisitor.getRoot()
      ));
    }

    @Override
    public void visitCaseStatement(CaseStatement caseStatement) {
      TreeVisitor visitor = new TreeVisitor();
      caseStatement.getCode().visit(visitor);
      root.addChild(new CaseNode(visitor.getRoot()));
    }

    @Override
    public void visitBreakStatement(BreakStatement breakStatement) {
      root.addChild(new BreakNode());
    }

    @Override
    public void visitContinueStatement(ContinueStatement continueStatement) {
      root.addChild(new ContinueNode());
    }

    @Override
    public void visitThrowStatement(ThrowStatement throwStatement) {
      TreeVisitor visitor = new TreeVisitor();
      throwStatement.getExpression().visit(visitor);
      root.addChild(new ThrowNode(visitor.getRoot()));
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement synchronizedStatement) {
      TreeVisitor exVisitor = new TreeVisitor();
      synchronizedStatement.getExpression().visit(exVisitor);

      TreeVisitor stmtVisitor = new TreeVisitor();
      synchronizedStatement.getCode().visit(stmtVisitor);

      root.addChild(new SynchronizedNode(exVisitor.getRoot(), stmtVisitor.getRoot()));
    }

    @Override
    public void visitCatchStatement(CatchStatement catchStatement) {
      TreeVisitor visitor = new TreeVisitor();
      catchStatement.getCode().visit(visitor);
      root.addChild(new CatchNode(visitor.getRoot()));
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCallExpression) {
      root.addChild(new MethodCallNode(methodCallExpression.getText()));
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression
                                                    staticMethodCallExpression) {
      root.addChild(new StaticMethodCallNode(staticMethodCallExpression.getText()));
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression
                                                   constructorCallExpression) {
      root.addChild(new ConstructorCallNode(constructorCallExpression.getText()));
    }

    @Override
    public void visitTernaryExpression(TernaryExpression ternaryExpression) {
      root.addChild(new TernaryNode(ternaryExpression.getText()));
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression elvisOperatorExpression) {
      root.addChild(new ShortTernaryNode());
    }

    @Override
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
      TreeVisitor leftVisitor = new TreeVisitor();
      binaryExpression.getLeftExpression().visit(leftVisitor);

      TreeVisitor rightVisitor = new TreeVisitor();
      binaryExpression.getRightExpression().visit(rightVisitor);

      root.addChild(new BinaryNode(
          leftVisitor.getRoot(),
          binaryExpression.getOperation().getText(),
          rightVisitor.getRoot()));
    }

    @Override
    public void visitPrefixExpression(PrefixExpression prefixExpression) {
      root.addChild(new PrefixNode(prefixExpression.getText()));
    }

    @Override
    public void visitPostfixExpression(PostfixExpression postfixExpression) {
      root.addChild(new PostfixNode(postfixExpression.getText()));
    }

    @Override
    public void visitBooleanExpression(BooleanExpression booleanExpression) {
      root.addChild(new BooleanNode(booleanExpression.getText()));
    }

    @Override
    public void visitClosureExpression(ClosureExpression closureExpression) {
      root.addChild(new ClosureNode(closureExpression.getText()));
    }

    @Override
    public void visitTupleExpression(TupleExpression tupleExpression) {
      root.addChild(new TupleNode(tupleExpression.getText()));
    }

    @Override
    public void visitMapExpression(MapExpression mapExpression) {
      root.addChild(new MapNode(mapExpression.getText()));
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression mapEntryExpression) {
      TreeVisitor keyVisitor = new TreeVisitor();
      mapEntryExpression.getKeyExpression().visit(keyVisitor);

      TreeVisitor valueVisitor = new TreeVisitor();
      mapEntryExpression.getValueExpression().visit(valueVisitor);

      root.addChild(new MapEntryNode(keyVisitor.getRoot(), valueVisitor.getRoot()));
    }

    @Override
    public void visitListExpression(ListExpression listExpression) {
      root.addChild(new ListNode(listExpression.getText()));
    }

    @Override
    public void visitRangeExpression(RangeExpression rangeExpression) {
      root.addChild(new RangeNode(rangeExpression.getText()));
    }

    @Override
    public void visitPropertyExpression(PropertyExpression propertyExpression) {
      root.addChild(new PropertyNode(propertyExpression.getText()));
    }

    @Override
    public void visitAttributeExpression(AttributeExpression attributeExpression) {
      root.addChild(new AttributeNode());
    }

    @Override
    public void visitFieldExpression(FieldExpression fieldExpression) {
      root.addChild(new FieldNode(fieldExpression.toString()));
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression methodPointerExpression) {
      root.addChild(new MethodPointerNode(methodPointerExpression.getText()));
    }

    @Override
    public void visitConstantExpression(ConstantExpression constantExpression) {
      root.addChild(new ConstantNode(constantExpression.toString()));
    }

    @Override
    public void visitClassExpression(ClassExpression classExpression) {
      root.addChild(new ClassExpressionNode(classExpression.getText()));
    }

    @Override
    public void visitVariableExpression(VariableExpression variableExpression) {
      root.addChild(new VariableNode(
          variableExpression.getType().getName(),
          variableExpression.getText()));
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression declarationExpression) {
      TreeVisitor leftVisitor = new TreeVisitor();
      declarationExpression.getVariableExpression().visit(leftVisitor);

      TreeVisitor rightVisitor = new TreeVisitor();
      declarationExpression.getRightExpression().visit(rightVisitor);

      root.addChild(new DeclarationNode(leftVisitor.getRoot(), rightVisitor.getRoot()));
    }

    @Override
    public void visitGStringExpression(GStringExpression gStringExpression) {
      root.addChild(new GStringNode(gStringExpression.getText()));
    }

    @Override
    public void visitArrayExpression(ArrayExpression arrayExpression) {
      root.addChild(new ArrayNode(arrayExpression.getText()));
    }

    @Override
    public void visitSpreadExpression(SpreadExpression spreadExpression) {
      root.addChild(new SpreadNode(spreadExpression.getText()));
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression spreadMapExpression) {
      root.addChild(new SpreadMapNode(spreadMapExpression.getText()));
    }

    @Override
    public void visitNotExpression(NotExpression notExpression) {
      root.addChild(new NotNode());
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression unaryMinusExpression) {
      root.addChild(new UnaryMinusNode(unaryMinusExpression.getText()));
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression unaryPlusExpression) {
      root.addChild(new UnaryPlusNode(unaryPlusExpression.getText()));
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression
                                                   bitwiseNegationExpression) {
      root.addChild(new BitwiseNegationNode(bitwiseNegationExpression.getText()));
    }

    @Override
    public void visitCastExpression(CastExpression castExpression) {
      root.addChild(new CastNode(castExpression.getText()));
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression argumentListExpression) {
      root.addChild(new ArgumentListNode());
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression closureListExpression) {
      root.addChild(new ClosureListNode(closureListExpression.getText()));
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression bytecodeExpression) {
      root.addChild(new BytecodeNode());
    }

  }

}
