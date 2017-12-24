package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class BitwiseNegationNode extends ASTNode {

  private String expression;

  public BitwiseNegationNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
