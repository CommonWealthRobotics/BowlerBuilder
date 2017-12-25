package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ReturnNode extends ASTNode {

  private String expression;

  public ReturnNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
