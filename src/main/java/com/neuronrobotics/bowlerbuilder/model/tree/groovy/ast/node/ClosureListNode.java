package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ClosureListNode extends ASTNode {

  private final String expression;

  public ClosureListNode(String expression) {
    super(ASTNodeType.ClosureListNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
