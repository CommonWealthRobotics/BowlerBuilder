package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ArrayNode extends ASTNode {

  private final String expression;

  public ArrayNode(String expression) {
    super(ASTNodeType.ArrayNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
