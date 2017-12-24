package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class BinaryNode extends ASTNode {

  private ASTNode leftExpression;
  private ASTNode rightExpression;

  public BinaryNode(ASTNode leftExpression, ASTNode rightExpression) {
    super();
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public ASTNode getLeftExpression() {
    return leftExpression;
  }

  public ASTNode getRightExpression() {
    return rightExpression;
  }

}
