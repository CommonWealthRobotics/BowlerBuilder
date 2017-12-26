package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class UnaryPlusNode extends ASTNode {

  private String expression;

  public UnaryPlusNode(String expression) {
    super(ASTNodeType.UnaryPlusNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
