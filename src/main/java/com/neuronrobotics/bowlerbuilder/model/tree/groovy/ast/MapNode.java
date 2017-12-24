package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class MapNode extends ASTNode {

  private String expression;

  public MapNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
