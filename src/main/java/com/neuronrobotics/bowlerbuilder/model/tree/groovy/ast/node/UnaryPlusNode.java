package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class UnaryPlusNode extends ASTNode {

  private final String expression;

  public UnaryPlusNode(String expression) {
    super(ASTNodeType.UnaryPlusNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
