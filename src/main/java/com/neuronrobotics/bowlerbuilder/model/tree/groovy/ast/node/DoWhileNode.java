package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class DoWhileNode extends ASTNode {

  private String expression;

  public DoWhileNode(String expression) {
    super(ASTNodeType.DoWhileNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
