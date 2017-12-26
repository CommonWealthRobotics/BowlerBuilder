package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class BinaryNode extends ASTNode {

  private ASTNode leftExpression;
  private String operation;
  private ASTNode rightExpression;

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
