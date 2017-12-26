package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class SynchronizedNode extends ASTNode {

  private final ASTNode expressionNode;
  private final ASTNode blockNode;

  public SynchronizedNode(ASTNode expressionNode, ASTNode blockNode) {
    super(ASTNodeType.SynchronizedNode);
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
