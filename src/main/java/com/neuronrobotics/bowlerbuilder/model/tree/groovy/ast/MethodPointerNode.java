package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class MethodPointerNode extends ASTNode {

  private String expression;

  public MethodPointerNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
