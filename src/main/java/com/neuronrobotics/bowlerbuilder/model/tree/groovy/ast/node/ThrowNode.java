package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ThrowNode extends ASTNode {

  private ASTNode expressionNode;

  public ThrowNode(ASTNode expressionNode) {
    super(ASTNodeType.ThrowNode);
    this.expressionNode = expressionNode;
  }

  public ASTNode getExpressionNode() {
    return expressionNode;
  }

}
