package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class SpreadNode extends ASTNode {

  private String expression;

  public SpreadNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
