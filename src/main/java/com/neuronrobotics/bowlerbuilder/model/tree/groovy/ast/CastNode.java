package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class CastNode extends ASTNode {

  private String expression;

  public CastNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
