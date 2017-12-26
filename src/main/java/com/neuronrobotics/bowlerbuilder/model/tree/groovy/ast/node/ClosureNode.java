package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ClosureNode extends ASTNode {

  private final String expression;

  public ClosureNode(String expression) {
    super(ASTNodeType.ClosureNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
