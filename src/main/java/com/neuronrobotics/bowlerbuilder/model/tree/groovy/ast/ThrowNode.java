package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ThrowNode extends ASTNode {

  private ASTNode expressionNode;

  public ThrowNode(ASTNode expressionNode) {
    super();
    this.expressionNode = expressionNode;
  }

  public ASTNode getExpressionNode() {
    return expressionNode;
  }

}
