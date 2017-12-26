package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class TupleNode extends ASTNode {

  private final String expression;

  public TupleNode(String expression) {
    super(ASTNodeType.TupleNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
