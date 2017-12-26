package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class PropertyNode extends ASTNode {

  private final String expression;

  public PropertyNode(String expression) {
    super(ASTNodeType.PropertyNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
