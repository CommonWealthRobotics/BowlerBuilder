package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class MapNode extends ASTNode {

  private final String expression;

  public MapNode(String expression) {
    super(ASTNodeType.MapEntryNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
