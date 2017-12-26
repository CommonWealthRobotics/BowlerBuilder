package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class StaticMethodCallNode extends ASTNode {

  private String expression;

  public StaticMethodCallNode(String expression) {
    super(ASTNodeType.StaticMethodCallNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
