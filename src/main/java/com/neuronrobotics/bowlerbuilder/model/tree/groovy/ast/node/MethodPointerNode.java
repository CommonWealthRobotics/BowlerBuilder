package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class MethodPointerNode extends ASTNode {

  private String expression;

  public MethodPointerNode(String expression) {
    super(ASTNodeType.MethodPointerNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
