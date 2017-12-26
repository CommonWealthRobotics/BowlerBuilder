package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ListNode extends ASTNode {

  private final String expression;

  public ListNode(String expression) {
    super(ASTNodeType.ListNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
