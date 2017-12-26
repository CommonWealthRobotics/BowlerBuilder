package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class PropertyNode extends ASTNode {

  private String expression;

  public PropertyNode(String expression) {
    super(ASTNodeType.PropertyNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
