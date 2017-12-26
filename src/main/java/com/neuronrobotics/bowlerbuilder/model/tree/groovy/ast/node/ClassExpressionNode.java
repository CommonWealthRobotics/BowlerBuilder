package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ClassExpressionNode extends ASTNode {

  private String expression;

  public ClassExpressionNode(String expression) {
    super(ASTNodeType.ClassExpressionNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
