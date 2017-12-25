package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class MethodCallNode extends ASTNode {

  private String expression;

  public MethodCallNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
