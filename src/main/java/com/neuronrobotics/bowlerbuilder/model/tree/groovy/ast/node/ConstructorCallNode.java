package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ConstructorCallNode extends ASTNode {

  private String expression;

  public ConstructorCallNode(String expression) {
    super(ASTNodeType.ConstructorCallNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
