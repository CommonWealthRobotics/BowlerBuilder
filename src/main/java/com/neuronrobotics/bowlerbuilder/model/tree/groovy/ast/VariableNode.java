package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class VariableNode extends ASTNode {

  private String dataType;
  private String name;

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
