package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class BooleanNode extends ASTNode {

  private final String expression;

  public BooleanNode(String expression) {
    super(ASTNodeType.BooleanNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
