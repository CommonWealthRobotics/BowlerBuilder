package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class AssertNode extends ASTNode {

  private String booleanExpression;
  private String messageExpression;

  public AssertNode(String booleanExpression, String messageExpression) {
    super(ASTNodeType.AssertNode);
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
