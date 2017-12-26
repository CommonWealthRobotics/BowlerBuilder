package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class UnaryMinusNode extends ASTNode {

  private String expression;

  public UnaryMinusNode(String expression) {
    super(ASTNodeType.UnaryMinusNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
