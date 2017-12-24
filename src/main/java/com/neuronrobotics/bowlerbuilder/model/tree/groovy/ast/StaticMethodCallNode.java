package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class StaticMethodCallNode extends ASTNode {

  private String expression;

  public StaticMethodCallNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
