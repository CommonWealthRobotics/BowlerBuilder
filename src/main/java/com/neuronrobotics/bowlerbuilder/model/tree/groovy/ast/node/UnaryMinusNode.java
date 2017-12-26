package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class UnaryMinusNode extends ASTNode {

  private String expression;

  public UnaryMinusNode(String expression) {
    super(ASTNodeType.UnaryMinusNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
