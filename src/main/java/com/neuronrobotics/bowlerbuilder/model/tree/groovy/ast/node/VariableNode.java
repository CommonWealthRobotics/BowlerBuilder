package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class VariableNode extends ASTNode {

  private final String dataType;
  private final String name;

  public VariableNode(String dataType, String name) {
    super(ASTNodeType.VariableNode);
    this.dataType = dataType;
    this.name = name;
  }

  public String getDataType() {
    return dataType;
  }

  public String getName() {
    return name;
  }

}
