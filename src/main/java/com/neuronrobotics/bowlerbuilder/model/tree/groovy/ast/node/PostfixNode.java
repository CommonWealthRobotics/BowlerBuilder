package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class PostfixNode extends ASTNode {

  private final String expression;

  public PostfixNode(String expression) {
    super(ASTNodeType.PostfixNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
