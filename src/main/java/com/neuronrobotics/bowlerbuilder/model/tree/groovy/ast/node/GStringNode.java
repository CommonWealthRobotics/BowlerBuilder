package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class GStringNode extends ASTNode {

  private String expression;

  public GStringNode(String expression) {
    super(ASTNodeType.GStringNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
