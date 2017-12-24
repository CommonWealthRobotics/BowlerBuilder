package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class GStringNode extends ASTNode {

  private String expression;

  public GStringNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
