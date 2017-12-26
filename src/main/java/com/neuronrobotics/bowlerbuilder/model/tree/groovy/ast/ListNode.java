package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ListNode extends ASTNode {

  private String expression;

  public ListNode(String expression) {
    super(ASTNodeType.ListNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
