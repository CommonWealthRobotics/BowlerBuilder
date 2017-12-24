package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class SynchronizedNode extends ASTNode {

  private ASTNode expressionNode;
  private ASTNode blockNode;

  public SynchronizedNode(ASTNode expressionNode, ASTNode blockNode) {
    super();
    this.expressionNode = expressionNode;
    this.blockNode = blockNode;
  }

  public ASTNode getExpressionNode() {
    return expressionNode;
  }

  public ASTNode getBlockNode() {
    return blockNode;
  }

}
