package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class SpreadNode extends ASTNode {

  private final String expression;

  public SpreadNode(String expression) {
    super(ASTNodeType.SpreadNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
