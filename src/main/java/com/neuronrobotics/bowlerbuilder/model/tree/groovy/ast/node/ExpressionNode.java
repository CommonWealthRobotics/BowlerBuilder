package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ExpressionNode extends ASTNode {

  private final ASTNode statementNode;

  public ExpressionNode(ASTNode statementNode) {
    super(ASTNodeType.ExpressionNode);
    this.statementNode = statementNode;
  }

  public ASTNode getStatementNode() {
    return statementNode;
  }

}
