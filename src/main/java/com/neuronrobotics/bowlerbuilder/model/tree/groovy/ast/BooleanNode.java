package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class BooleanNode extends ASTNode {

  private String expression;

  public BooleanNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
