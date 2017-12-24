package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ClosureListNode extends ASTNode {

  private String expression;

  public ClosureListNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
