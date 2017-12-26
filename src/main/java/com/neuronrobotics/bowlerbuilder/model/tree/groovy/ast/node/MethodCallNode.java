package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class MethodCallNode extends ASTNode {

  private String expression;

  public MethodCallNode(String expression) {
    super(ASTNodeType.MethodCallNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
