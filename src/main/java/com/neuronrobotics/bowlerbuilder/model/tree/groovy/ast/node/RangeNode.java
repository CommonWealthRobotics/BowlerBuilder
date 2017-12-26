package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class RangeNode extends ASTNode {

  private String expression;

  public RangeNode(String expression) {
    super(ASTNodeType.RangeNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
