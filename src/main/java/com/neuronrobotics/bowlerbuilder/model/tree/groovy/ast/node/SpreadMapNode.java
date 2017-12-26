package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class SpreadMapNode extends ASTNode {

  private final String expression;

  public SpreadMapNode(String expression) {
    super(ASTNodeType.SpreadMapNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
