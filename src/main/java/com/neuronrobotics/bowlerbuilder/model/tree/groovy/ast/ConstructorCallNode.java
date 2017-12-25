package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ConstructorCallNode extends ASTNode {

  private String expression;

  public ConstructorCallNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
