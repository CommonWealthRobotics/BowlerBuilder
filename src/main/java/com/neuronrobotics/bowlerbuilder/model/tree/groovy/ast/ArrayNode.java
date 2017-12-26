package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ArrayNode extends ASTNode {

  private String expression;

  public ArrayNode(String expression) {
    super(ASTNodeType.ArrayNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
