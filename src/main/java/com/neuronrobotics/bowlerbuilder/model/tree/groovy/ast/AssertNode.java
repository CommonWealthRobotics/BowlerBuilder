package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class AssertNode extends ASTNode {

  private String booleanExpression;
  private String messageExpression;

  public AssertNode(String booleanExpression, String messageExpression) {
    super();
    this.booleanExpression = booleanExpression;
    this.messageExpression = messageExpression;
  }

  public String getBooleanExpression() {
    return booleanExpression;
  }

  public String getMessageExpression() {
    return messageExpression;
  }

}
