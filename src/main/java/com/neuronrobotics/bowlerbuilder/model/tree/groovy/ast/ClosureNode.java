package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ClosureNode extends ASTNode {

  private String expression;

  public ClosureNode(String expression) {
    super(ASTNodeType.ClosureNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
