package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ConstantNode extends ASTNode {

  private String expression;

  public ConstantNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
