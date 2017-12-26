package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class BinaryNode extends ASTNode {

  private final ASTNode leftExpression;
  private final String operation;
  private final ASTNode rightExpression;

  public BinaryNode(ASTNode leftExpression, String operation, ASTNode rightExpression) {
    super(ASTNodeType.BinaryNode);
    this.leftExpression = leftExpression;
    this.operation = operation;
    this.rightExpression = rightExpression;
  }

  public ASTNode getLeftExpression() {
    return leftExpression;
  }

  public String getOperation() {
    return operation;
  }

  public ASTNode getRightExpression() {
    return rightExpression;
  }

}
