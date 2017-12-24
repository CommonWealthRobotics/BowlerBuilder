package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class SpreadMapNode extends ASTNode {

  private String expression;

  public SpreadMapNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
