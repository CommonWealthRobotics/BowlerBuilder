package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class FieldNode extends ASTNode {

  private String expression;

  public FieldNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
