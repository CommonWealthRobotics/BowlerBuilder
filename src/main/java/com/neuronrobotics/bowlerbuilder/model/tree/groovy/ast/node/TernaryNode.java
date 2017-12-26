package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class TernaryNode extends ASTNode {

  private final String expression;

  public TernaryNode(String expression) {
    super(ASTNodeType.TernaryNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
