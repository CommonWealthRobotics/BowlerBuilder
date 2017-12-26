package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class BitwiseNegationNode extends ASTNode {

  private String expression;

  public BitwiseNegationNode(String expression) {
    super(ASTNodeType.BitwiseNegationNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
