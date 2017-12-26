package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class WhileNode extends ASTNode {

  private String expression;

  public WhileNode(String expression) {
    super(ASTNodeType.WhileNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
