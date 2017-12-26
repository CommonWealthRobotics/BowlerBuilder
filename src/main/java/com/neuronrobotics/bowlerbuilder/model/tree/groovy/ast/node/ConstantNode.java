package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ConstantNode extends ASTNode {

  private final String expression;

  public ConstantNode(String expression) {
    super(ASTNodeType.ConstantNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
