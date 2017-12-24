package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class PostfixNode extends ASTNode {

  private String expression;

  public PostfixNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
