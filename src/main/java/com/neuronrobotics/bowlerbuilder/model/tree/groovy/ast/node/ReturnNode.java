package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ReturnNode extends ASTNode {

  private final String expression;

  public ReturnNode(String expression) {
    super(ASTNodeType.ReturnNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
