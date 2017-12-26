package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class CastNode extends ASTNode {

  private final String expression;

  public CastNode(String expression) {
    super(ASTNodeType.CastNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
