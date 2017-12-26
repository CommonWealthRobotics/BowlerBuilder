package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class FieldNode extends ASTNode {

  private String expression;

  public FieldNode(String expression) {
    super(ASTNodeType.FieldNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
