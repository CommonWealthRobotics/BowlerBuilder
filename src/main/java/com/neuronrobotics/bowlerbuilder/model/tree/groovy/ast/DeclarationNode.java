package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class DeclarationNode extends ASTNode {

  private ASTNode leftExpression;
  private ASTNode rightExpression;

  public DeclarationNode(ASTNode leftExpression, ASTNode rightExpression) {
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
